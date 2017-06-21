package com.rbardini.carteiro.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

abstract class Tracker {
  static final int MAX_ITEMS_PER_REQ = 5000;
  static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
  private static final Pattern SEPARATOR_PATTERN = Pattern.compile("^(.+?) - ");
  private static final Pattern OBJETO_PATTERN = Pattern.compile("^Objeto");
  private static final Pattern CONTEUDO_PATTERN = Pattern.compile("(e?/?(ou)?\\s?)conteúdo");
  private static final Pattern COMMA_PATTERN = Pattern.compile(",");
  private static final Pattern PERIOD_PATTERN = Pattern.compile("\\.$");

  static List<String[]> chunk(String[] cods, int size) {
    List<String[]> chunks = new ArrayList<>();
    int length = cods.length;

    for (int i = 0; i < length; i += size) {
      chunks.add(Arrays.copyOfRange(cods, i, Math.min(length, i + size)));
    }

    return chunks;
  }

  static String normalizeString(String str) {
    if (str == null) return null;
    return WHITESPACE_PATTERN.matcher(str).replaceAll(" ").trim();
  }

  static String formatStatus(String status) {
    status = SEPARATOR_PATTERN.matcher(status).replaceAll("");
    status = OBJETO_PATTERN.matcher(status).replaceAll("");
    status = CONTEUDO_PATTERN.matcher(status).replaceAll("");
    status = COMMA_PATTERN.matcher(status).replaceAll("");
    status = PERIOD_PATTERN.matcher(status).replaceAll("");
    status = status.trim();

    return status.substring(0, 1).toUpperCase() + status.substring(1);
  }

  static String formatInfo(String info) {
    if (info == null) return null;
    return PERIOD_PATTERN.matcher(info).replaceAll("").trim();
  }

  static String buildLocation(String local, String cidade, String uf, String bairro) {
    if (local != null && cidade != null && uf != null) {
      local += " - ";

      if (bairro != null && !bairro.equals(cidade)) {
        local += bairro + ", ";
      }

      local += cidade + "/" + uf;
    }

    return local;
  }
}
