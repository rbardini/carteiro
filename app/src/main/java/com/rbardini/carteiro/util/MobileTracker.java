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
  private static final String REQ_RESULT = "T";
  private static final String REQ_LANGUAGE = "101";
  private static final String REQ_TOKEN = "QTXFMvu_Z-6XYezP3VbDsKBgSeljSqIysM9x";
  private static final int REQ_TIMEOUT = 60000;

  public static List<Shipment> track(final String[] cods, Context context) throws IOException {
    List<Shipment> shipments = new ArrayList<>();

    if (cods.length == 0) return shipments;

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
        return buildRequestBody(cods).getBytes();
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
                local = buildLocation(destino.getJSONObject(0));
                if (local != null) detalhe = "Em trânsito para " + local;
              }
            }

            records.add(new ShipmentRecord(date, formatStatus(descricao), local, formatInfo(detalhe)));
          }
        }

        Shipment shipment = new Shipment(cod);
        shipment.addRecords(records);
        shipments.add(shipment);
      }

    } catch (InterruptedException | ExecutionException | TimeoutException | JSONException e) {
      throw new IOException(PostalUtils.Error.NET_ERROR);
    }

    return shipments;
  }

  public static Shipment track(String cod, Context context) throws IOException {
    List<Shipment> shipments = track(new String[] {cod}, context);
    return shipments.get(0);
  }

  private static String buildRequestBody(String[] cods) {
    return (
      "<rastroObjeto>" +
        "<usuario>" + REQ_USERNAME + "</usuario>" +
        "<senha>" + REQ_PASSWORD + "</senha>" +
        "<tipo>" + REQ_TYPE + "</tipo>" +
        "<resultado>" + REQ_RESULT + "</resultado>" +
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
