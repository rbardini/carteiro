package com.rbardini.carteiro.util;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class MobileTracker extends Tracker {
  private static final String URL = "http://webservice.correios.com.br/service/rest/rastro/rastroMobile";
  private static final String REQ_CONTENT_TYPE = "application/xml";
  private static final String REQ_ACCEPT_TYPE = "application/json";
  private static final String REQ_USERNAME = "MobileXect";
  private static final String REQ_PASSWORD = "DRW0#9F$@0";
  private static final String REQ_TYPE = "L";
  private static final String REQ_RESULT_ALL = "T";
  private static final String REQ_RESULT_LAST = "U";
  private static final String REQ_LANGUAGE = "101";
  private static final String REQ_TOKEN = "QTXFMvu_Z-6XYezP3VbDsKBgSeljSqIysM9x";
  private static final int REQ_TIMEOUT = 60000;

  private static List<Shipment> track(final List<Shipment> shipments, Context context, final boolean shallow) throws IOException {
    if (shipments.size() == 0) return shipments;

    final Map<String, Shipment> shipmentMap = buildShipmentMap(shipments);

    RequestQueue queue = Volley.newRequestQueue(context);
    RequestFuture<JSONObject> future = RequestFuture.newFuture();
    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, null, future, future) {
      @Override
      public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", REQ_ACCEPT_TYPE);
        return headers;
      }

      @Override
      public String getBodyContentType() {
        return REQ_CONTENT_TYPE;
      }

      @Override
      public byte[] getBody() {
        return buildRequestBody(shipmentMap, shallow).getBytes();
      }
    };

    request.setRetryPolicy(new DefaultRetryPolicy(REQ_TIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    queue.add(request);

    try {
      JSONObject response = future.get(REQ_TIMEOUT, TimeUnit.MILLISECONDS);
      JSONArray objetos = response.getJSONArray("objeto");

      for (int i = objetos.length() - 1; i >= 0; i--) {
        JSONObject objeto = objetos.getJSONObject(i);

        String cod = objeto.getString("numero");
        List<ShipmentRecord> records = new ArrayList<>();

        if (!objeto.getString("categoria").startsWith("ERRO")) {
          JSONArray eventos = objeto.getJSONArray("evento");

          for (int j = eventos.length() - 1; j >= 0; j--) {
            JSONObject evento = eventos.getJSONObject(j);

            String data = normalizeString(evento.optString("data", null));
            String hora = normalizeString(evento.optString("hora", null));
            String descricao = normalizeString(evento.optString("descricao", null));
            String local = buildLocation(evento.optJSONObject("unidade"));
            String detalhe = normalizeString(evento.optString("detalhe", null));

            Date date;

            try {
              date = DATE_FORMAT.parse(data + " " + hora);
            } catch (ParseException e) {
              continue;
            }

            if (detalhe == null) {
              JSONArray destino = evento.optJSONArray("destino");

              if (destino != null) {
                String localDestino = buildLocation(destino.getJSONObject(0));
                if (localDestino != null) detalhe = "Em trânsito para " + localDestino;
              }
            }

            records.add(new ShipmentRecord(date, formatStatus(descricao), local, formatInfo(detalhe)));
          }
        }

        Shipment shipment = shipmentMap.get(cod);
        shipment.replaceRecords(records);
      }

    } catch (InterruptedException | ExecutionException | TimeoutException | JSONException e) {
      throw new IOException(PostalUtils.Error.NET_ERROR);
    }

    return shipments;
  }

  public static List<Shipment> shallowTrack(final List<Shipment> shipments, Context context) throws IOException {
    return track(shipments, context, true);
  }

  public static List<Shipment> deepTrack(final List<Shipment> shipments, Context context) throws IOException {
    return track(shipments, context, false);
  }

  private static Shipment track(Shipment shipment, Context context, boolean shallow) throws IOException {
    List<Shipment> shipments = new ArrayList<>();
    shipments.add(shipment);

    return track(shipments, context, shallow).get(0);
  }

  public static Shipment shallowTrack(Shipment shipment, Context context) throws IOException {
    return track(shipment, context, true);
  }

  public static Shipment deepTrack(Shipment shipment, Context context) throws IOException {
    return track(shipment, context, false);
  }

  private static Shipment track(String cod, Context context, boolean shallow) throws IOException {
    return track(new Shipment(cod), context, shallow);
  }

  public static Shipment shallowTrack(String cod, Context context) throws IOException {
    return track(cod, context, true);
  }

  public static Shipment deepTrack(String cod, Context context) throws IOException {
    return track(cod, context, false);
  }

  private static Map<String, Shipment> buildShipmentMap(List<Shipment> shipments) {
    Map<String, Shipment> shipmentMap = new HashMap<>();

    for (Shipment shipment : shipments) {
      shipmentMap.put(shipment.getNumber(), shipment);
    }

    return shipmentMap;
  }

  private static String buildRequestBody(Map<String, Shipment> shipmentMap, boolean shallow) {
    String[] cods = shipmentMap.keySet().toArray(new String[0]);

    return (
      "<rastroObjeto>" +
        "<usuario>" + REQ_USERNAME + "</usuario>" +
        "<senha>" + REQ_PASSWORD + "</senha>" +
        "<tipo>" + REQ_TYPE + "</tipo>" +
        "<resultado>" + (shallow ? REQ_RESULT_LAST : REQ_RESULT_ALL) + "</resultado>" +
        "<objetos>" + TextUtils.join("", cods) + "</objetos>" +
        "<lingua>" + REQ_LANGUAGE + "</lingua>" +
        "<token>" + REQ_TOKEN + "</token>" +
      "</rastroObjeto>"
    );
  }

  private static String buildLocation(JSONObject obj) {
    String local, cidade, uf, bairro = null;

    local = normalizeString(obj.optString("local", null));
    cidade = normalizeString(obj.optString("cidade", null));
    uf = normalizeString(obj.optString("uf", null));

    JSONObject endereco = obj.optJSONObject("endereco");
    if (endereco != null) {
      bairro = normalizeString(endereco.optString("bairro", null));
    }

    return buildLocation(local, cidade, uf, bairro);
  }
}
