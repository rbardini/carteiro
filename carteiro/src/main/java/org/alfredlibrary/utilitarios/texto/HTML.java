/*
 *  This file is part of Alfred Library.
 *
 *  Alfred Library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Alfred Library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Alfred Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfredlibrary.utilitarios.texto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitário para HTML.
 *
 * @author Marlon Silva Carvalho
 * @since 10/05/2010
 */
final public class HTML {

  private HTML() {
  }

  /**
   * Encontrar e retornar todos os links encontrados no texto.
   *
   * @param texto
   *            Texto.
   * @return Array de links encontrados.
   */
  public static String[] acharLinks(String texto) {
    Collection<String> encontrados = new ArrayList<String>();
    String pattern = "https?://([-\\w\\.]+)+(:\\d+)?(/([\\w/_\\.]*(\\?\\S+)?)?)?";
    Pattern padrao = Pattern.compile(pattern);
    Matcher pesquisa = padrao.matcher(texto);
    while (pesquisa.find()) {
      encontrados.add(pesquisa.group());
    }
    return encontrados.toArray(new String[] {});
  }

  /**
   * Remover todas as tags de um texto.
   *
   * @param texto
   *            Texto que terá as tags removidas.
   * @return Texto com as tags removidas.
   */
  public static String removerTags(String texto) {
    String noHTMLString = texto.replaceAll("\\<.*?\\>", "");
    return noHTMLString;
  }

  /**
   * Substituir do texto os elementos HTML especiais, como &nbsp;, pelo valor
   * correspondente em ASCII. Código original em
   * http://www.rgagnon.com/javadetails/java-0307.html.
   *
   * @param source
   *            Código que terá o texto trocado.
   * @param start
   *            Onde iniciar a troca.
   * @return Texto com os valores convertidos.
   */
  public static String desconverterElementosHTMLEspeciais(String source, int start) {
    HashMap<String, String> htmlEntities;
    htmlEntities = new HashMap<String, String>();
    htmlEntities.put("&lt;", "<");
    htmlEntities.put("&gt;", ">");
    htmlEntities.put("&amp;", "&");
    htmlEntities.put("&quot;", "\"");
    htmlEntities.put("&agrave;", "à");
    htmlEntities.put("&Agrave;", "À");
    htmlEntities.put("&atilde;", "ã");
    htmlEntities.put("&Atilde;", "Ã");
    htmlEntities.put("&aacute;", "á");
    htmlEntities.put("&Aacute;", "Á");
    htmlEntities.put("&acirc;", "â");
    htmlEntities.put("&auml;", "ä");
    htmlEntities.put("&Auml;", "Ä");
    htmlEntities.put("&Acirc;", "Â");
    htmlEntities.put("&aring;", "å");
    htmlEntities.put("&Aring;", "Å");
    htmlEntities.put("&aelig;", "æ");
    htmlEntities.put("&AElig;", "Æ");
    htmlEntities.put("&ccedil;", "ç");
    htmlEntities.put("&Ccedil;", "Ç");
    htmlEntities.put("&eacute;", "é");
    htmlEntities.put("&Eacute;", "É");
    htmlEntities.put("&egrave;", "è");
    htmlEntities.put("&Egrave;", "È");
    htmlEntities.put("&ecirc;", "ê");
    htmlEntities.put("&Ecirc;", "Ê");
    htmlEntities.put("&euml;", "ë");
    htmlEntities.put("&Euml;", "Ë");
    htmlEntities.put("&iuml;", "ï");
    htmlEntities.put("&Iuml;", "Ï");
    htmlEntities.put("&iacute;", "í");
    htmlEntities.put("&Iacute;", "Í");
    htmlEntities.put("&ocirc;", "ô");
    htmlEntities.put("&Ocirc;", "Ô");
    htmlEntities.put("&otilde;", "õ");
    htmlEntities.put("&Otilde;", "Õ");
    htmlEntities.put("&oacute;", "ó");
    htmlEntities.put("&Oacute;", "Ó");
    htmlEntities.put("&uacute;", "ú");
    htmlEntities.put("&Uacute;", "Ú");
    htmlEntities.put("&ouml;", "ö");
    htmlEntities.put("&Ouml;", "Ö");
    htmlEntities.put("&oslash;", "ø");
    htmlEntities.put("&Oslash;", "Ø");
    htmlEntities.put("&szlig;", "ß");
    htmlEntities.put("&ugrave;", "ù");
    htmlEntities.put("&Ugrave;", "Ù");
    htmlEntities.put("&ucirc;", "û");
    htmlEntities.put("&Ucirc;", "Û");
    htmlEntities.put("&uuml;", "ü");
    htmlEntities.put("&Uuml;", "Ü");
    htmlEntities.put("&nbsp;", " ");
    htmlEntities.put("&copy;", "\u00a9");
    htmlEntities.put("&reg;", "\u00ae");
    htmlEntities.put("&euro;", "\u20a0");
    int i, j;
    i = source.indexOf("&", start);
    if (i > -1) {
      j = source.indexOf(";", i);
      if (j > i) {
        String entityToLookFor = source.substring(i, j + 1);
        String value = htmlEntities.get(entityToLookFor);
        if (value != null) {
          source = new StringBuffer().append(source.substring(0, i))
              .append(value).append(source.substring(j + 1))
              .toString();
        }
        return desconverterElementosHTMLEspeciais(source, i + 1);
      }
    }
    return source;
  }

  /**
   * Converte caracteres especiais para elementos HTML. Código "gentilmente sugado" do site
   *
   * @param source String que terá os elementos substituídos.
   * @return Texto formatado.
   */
  public static final String converterParaElementosHTMLEspeciais(String source) {
    HashMap<Character, String> htmlEntities;
    htmlEntities = new HashMap<Character, String>();
    htmlEntities.put('<', "&lt;");
    htmlEntities.put('>', "&gt;");
    htmlEntities.put('&', "&amp;");
    htmlEntities.put('\\', "&quot;");
    htmlEntities.put('à', "&agrave;");
    htmlEntities.put('À', "&Agrave;");
    htmlEntities.put('ã', "&atilde;");
    htmlEntities.put('Ã', "&Atilde;");
    htmlEntities.put('á', "&aacute;");
    htmlEntities.put('Á', "&Aacute;");
    htmlEntities.put('â', "&acirc;");
    htmlEntities.put('ä', "&auml;");
    htmlEntities.put('Ä', "&Auml;");
    htmlEntities.put('Â', "&Acirc;");
    htmlEntities.put('å', "&aring;");
    htmlEntities.put('Å', "&Aring;");
    htmlEntities.put('æ', "&aelig;");
    htmlEntities.put('Æ', "&AElig;");
    htmlEntities.put('ç', "&ccedil;");
    htmlEntities.put('Ç', "&Ccedil;");
    htmlEntities.put('é', "&eacute;");
    htmlEntities.put('É', "&Eacute;");
    htmlEntities.put('è', "&egrave;");
    htmlEntities.put('È', "&Egrave;");
    htmlEntities.put('ê', "&ecirc;");
    htmlEntities.put('Ê', "&Ecirc;");
    htmlEntities.put('ë', "&euml;");
    htmlEntities.put('Ë', "&Euml;");
    htmlEntities.put('ï', "&iuml;");
    htmlEntities.put('Ï', "&Iuml;");
    htmlEntities.put('í', "&iacute;");
    htmlEntities.put('Í', "&Iacute;");
    htmlEntities.put('ô', "&ocirc;");
    htmlEntities.put('Ô', "&Ocirc;");
    htmlEntities.put('õ', "&otilde;");
    htmlEntities.put('Õ', "&Otilde;");
    htmlEntities.put('ó', "&oacute;");
    htmlEntities.put('Ó', "&Oacute;");
    htmlEntities.put('ú', "&uacute;");
    htmlEntities.put('Ú', "&Uacute;");
    htmlEntities.put('ö', "&ouml;");
    htmlEntities.put('Ö', "&Ouml;");
    htmlEntities.put('ø', "&oslash;");
    htmlEntities.put('Ø', "&Oslash;");
    htmlEntities.put('ß', "&szlig;");
    htmlEntities.put('ù', "&ugrave;");
    htmlEntities.put('Ù', "&Ugrave;");
    htmlEntities.put('û', "&ucirc;");
    htmlEntities.put('Û', "&Ucirc;");
    htmlEntities.put('ü', "&uuml;");
    htmlEntities.put('Ü', "&Uuml;");
    htmlEntities.put(' ', "&nbsp;");
    htmlEntities.put('\u00a9', "&copy;");
    htmlEntities.put('\u00ae', "&reg;");
    htmlEntities.put('\u20a0', "&euro;");
    int length = source.length();
    StringBuilder sb = new StringBuilder();
    for(int i=0; i < length; i++) {
      char ch = source.charAt(i);
      if ( htmlEntities.containsKey(ch) ) {
        String o = htmlEntities.get(ch);
        sb.append(o);
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

}
