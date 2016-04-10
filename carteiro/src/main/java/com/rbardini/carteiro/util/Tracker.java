package com.rbardini.carteiro.util;

import com.rbardini.carteiro.model.PostalRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class Tracker {
  public static DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
  public static int CONN_TIMEOUT = 15000;

  public static List<PostalRecord> track(String cod) throws IOException, ParseException {
    List<PostalRecord> prList = new ArrayList<>();
    String html = fetchPage(cod);

    if (html.contains("O nosso sistema não possui dados sobre o objeto")) {
      return prList;
    }

    if (!html.contains("O horário não indica quando a situação ocorreu")) {
      throw new IOException(PostalUtils.Error.NET_ERROR);
    }

    BufferedReader br = new BufferedReader(new StringReader(html));
    String line;

    try {
      while ((line = br.readLine()) != null) {
        if (line.contains("<tr><td ")) {
          int index = line.indexOf("rowspan=");

          if (index != -1) {
            PostalRecord pr = new PostalRecord(cod);
            index += 8;

            int rowSpan = Integer.parseInt(line.substring(index, index + 1));
            index += 2;

            pr.setDate(DATE_FORMAT.parse(line.substring(index, line.indexOf("</td><td>", index) + 1)));
            index = line.indexOf("</td><td>", index) + 9;

            pr.setLoc(line.substring(index, line.indexOf("</td><td>", index)).replaceAll("\\s+", " "));
            index = line.indexOf("</td><td>", index) + 30;

            pr.setStatus(line.substring(index, line.indexOf("</font></td></tr>", index)));

            if (pr.getStatus().trim().equals("")) {
              pr.setStatus(PostalUtils.Status.INDETERMINADO);
            }

            if (rowSpan > 1) {
              line = br.readLine();
              index = line.indexOf("colspan=") + 10;

              pr.setInfo(line.substring(index, line.indexOf("</td></tr>", index)).replaceAll("\\s+", " "));

              if (pr.getInfo().startsWith("Por favor, entre em contato")) {
                pr.setInfo("Entre em contato com os Correios");
              }
            }

            prList.add(pr);
          }
        }
      }

    } finally {
      br.close();
    }

    Collections.reverse(prList);

    return prList;
  }

  public static String fetchPage(String cod) throws IOException {
    try {
      URL url = new URL(String.format(PostalUtils.WEBSRO_URL, cod));
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(CONN_TIMEOUT);
      conn.setReadTimeout(CONN_TIMEOUT);

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "ISO-8859-1"));
      StringBuilder builder = new StringBuilder();
      String line;

      while ((line = br.readLine()) != null) {
        builder.append(line);
        builder.append(System.getProperty("line.separator"));
      }

      br.close();

      return builder.toString();

    } catch (IOException e) {
      throw new IOException(PostalUtils.Error.NET_ERROR);
    }
  }
}
