package com.rbardini.carteiro.util;

import com.rbardini.carteiro.model.PostalRecord;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Tracker {
  private static final String NAMESPACE = "http://resource.webservice.correios.com.br/";
  private static final String METHOD_NAME = "buscaEventosLista";
  private static final String SOAP_ACTION = METHOD_NAME;
  private static final String URL = "http://webservice.correios.com.br/service/rastro";
  private static final String REQ_USERNAME = "ECT";
  private static final String REQ_PASSWORD = "SRO";
  private static final String REQ_TYPE = "L";
  private static final String REQ_RESULT = "T";
  private static final String REQ_LANGUAGE = "101";

  private static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
  private static Pattern SEPARATOR_PATTERN = Pattern.compile("^(.+?) - ");
  private static Pattern OBJETO_PATTERN = Pattern.compile("^Objeto");
  private static Pattern CONTEUDO_PATTERN = Pattern.compile("(e?/?(ou)?\\s?)conteúdo");
  private static Pattern COMMA_PATTERN = Pattern.compile(",");
  private static Pattern PERIOD_PATTERN = Pattern.compile("\\.$");

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

  public static List<List<PostalRecord>> track(String[] cods) throws IOException, ParseException {
    List<List<PostalRecord>> prLists = new ArrayList<>();

    SoapSerializationEnvelope envelope = buildEnvelope(cods);
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
            String descricao = getStringProperty(evento, "descricao");;
            String local = buildLocation(evento);
            String detalhe = getStringProperty(evento, "detalhe");

            PostalRecord pr = new PostalRecord(cod, DATE_FORMAT.parse(data + " " + hora), formatStatus(descricao), local, formatInfo(detalhe));

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

    } catch (XmlPullParserException e) {
      throw new IOException(PostalUtils.Error.NET_ERROR);
    }

    return prLists;
  }

  public static List<PostalRecord> track(String cod) throws IOException, ParseException {
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

  private static String normalizeString(String str) {
    if (str != null) {
      return WHITESPACE_PATTERN.matcher(str).replaceAll(" ").trim();
    }

    return str;
  }

  private static String formatStatus(String status) {
    status = SEPARATOR_PATTERN.matcher(status).replaceAll("");
    status = OBJETO_PATTERN.matcher(status).replaceAll("");
    status = CONTEUDO_PATTERN.matcher(status).replaceAll("");
    status = COMMA_PATTERN.matcher(status).replaceAll("");
    status = PERIOD_PATTERN.matcher(status).replaceAll("");
    status = status.trim();

    return status.substring(0, 1).toUpperCase() + status.substring(1);
  }

  private static String formatInfo(String info) {
    if (info == null) return info;
    return PERIOD_PATTERN.matcher(info).replaceAll("").trim();
  }

  private static String buildLocation(SoapObject obj) {
    String local = getStringProperty(obj, "local");
    String bairro = getStringProperty(obj, "bairro");
    String cidade = getStringProperty(obj, "cidade");
    String uf = getStringProperty(obj, "uf");

    if (cidade != null && uf != null) {
      local += " - ";

      if (bairro != null) local += bairro + ", ";
      local += cidade + "/" + uf;
    }

    return local;
  }
}
