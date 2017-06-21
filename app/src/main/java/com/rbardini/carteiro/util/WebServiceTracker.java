package com.rbardini.carteiro.util;

import com.rbardini.carteiro.model.PostalRecord;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class WebServiceTracker extends Tracker {
  private static final String NAMESPACE = "http://resource.webservice.correios.com.br/";
  private static final String METHOD_NAME = "buscaEventosLista";
  private static final String SOAP_ACTION = METHOD_NAME;
  private static final String URL = "http://webservice.correios.com.br/service/rastro";
  private static final String REQ_USERNAME = "ECT";
  private static final String REQ_PASSWORD = "SRO";
  private static final String REQ_TYPE = "L";
  private static final String REQ_RESULT = "T";
  private static final String REQ_LANGUAGE = "101";

  public static List<List<PostalRecord>> track(String[] cods) throws IOException {
    List<List<PostalRecord>> prLists = new ArrayList<>();

    if (cods.length == 0) return prLists;

    for (String[] codsChunk : chunk(cods, MAX_ITEMS_PER_REQ)) {
      SoapSerializationEnvelope envelope = buildEnvelope(codsChunk);
      HttpTransportSE httpTransport = new HttpTransportSE(URL);

      try {
        httpTransport.call(SOAP_ACTION, envelope);
        SoapObject result = (SoapObject) envelope.getResponse();
        List<SoapObject> objetos = getPropertyValues(result, "objeto");

        for (SoapObject objeto : objetos) {
          String cod = getStringProperty(objeto, "numero");
          List<PostalRecord> prList = new ArrayList<>();

          if (!objeto.hasProperty("erro")) {
            List<SoapObject> eventos = getPropertyValues(objeto, "evento");

            for (SoapObject evento : eventos) {
              String data = getStringProperty(evento, "data");
              String hora = getStringProperty(evento, "hora");
              String descricao = getStringProperty(evento, "descricao");
              String local = buildLocation(evento);
              String detalhe = getStringProperty(evento, "detalhe");

              Date date;

              try {
                date = DATE_FORMAT.parse(data + " " + hora);
              } catch (ParseException e) {
                continue;
              }

              PostalRecord pr = new PostalRecord(cod, date, formatStatus(descricao), local, formatInfo(detalhe));

              if (detalhe == null) {
                SoapObject destino = getPropertyValue(evento, "destino");

                if (destino != null) {
                  local = buildLocation(destino);
                  if (local != null) pr.setInfo("Em trânsito para " + local);
                }
              }

              prList.add(pr);
            }
          }

          Collections.reverse(prList);
          prLists.add(prList);
        }

      } catch (Exception e) {
        throw new IOException(PostalUtils.Error.NET_ERROR);
      }
    }

    return prLists;
  }

  public static List<PostalRecord> track(String cod) throws IOException {
    List<List<PostalRecord>> prLists = track(new String[] {cod});
    return prLists.get(0);
  }

  private static SoapSerializationEnvelope buildEnvelope(String[] cods) {
    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
    request.addProperty("usuario", REQ_USERNAME);
    request.addProperty("senha", REQ_PASSWORD);
    request.addProperty("tipo", REQ_TYPE);
    request.addProperty("resultado", REQ_RESULT);
    request.addProperty("lingua", REQ_LANGUAGE);

    for (String cod : cods) {
      request.addProperty("objetos", cod);
    }

    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
    envelope.setOutputSoapObject(request);

    return envelope;
  }

  private static SoapObject getPropertyValue(SoapObject obj, String name) {
    int propertyCount = obj.getPropertyCount();

    for (int i = 0; i < propertyCount; i++) {
      PropertyInfo propertyInfo = obj.getPropertyInfo(i);

      if (propertyInfo.getName().equals(name)) {
        return (SoapObject) propertyInfo.getValue();
      }
    }

    return null;
  }

  private static List<SoapObject> getPropertyValues(SoapObject obj, String name) {
    List<SoapObject> result = new ArrayList<>();
    int propertyCount = obj.getPropertyCount();

    for (int i = 0; i < propertyCount; i++) {
      PropertyInfo propertyInfo = obj.getPropertyInfo(i);

      if (propertyInfo.getName().equals(name)) {
        SoapObject value = (SoapObject) propertyInfo.getValue();
        result.add(value);
      }
    }

    return result;
  }

  private static String getStringProperty(SoapObject obj, String name) {
    return normalizeString(obj.getPropertySafely(name).toString());
  }

  private static String buildLocation(SoapObject obj) {
    String local = getStringProperty(obj, "local");
    String bairro = getStringProperty(obj, "bairro");
    String cidade = getStringProperty(obj, "cidade");
    String uf = getStringProperty(obj, "uf");

    return buildLocation(local, cidade, uf, bairro);
  }
}
