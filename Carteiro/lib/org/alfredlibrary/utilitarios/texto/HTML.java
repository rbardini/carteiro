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
 * Utilit�rio para HTML.
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
   *            Texto que ter� as tags removidas.
   * @return Texto com as tags removidas.
   */
  public static String removerTags(String texto) {
    String noHTMLString = texto.replaceAll("\\<.*?\\>", "");
    return noHTMLString;
  }

  /**
   * Substituir do texto os elementos HTML especiais, como &nbsp;, pelo valor
   * correspondente em ASCII. C�digo original em
   * http://www.rgagnon.com/javadetails/java-0307.html.
   *
   * @param source
   *            C�digo que ter� o texto trocado.
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
    htmlEntities.put("&agrave;", "�");
    htmlEntities.put("&Agrave;", "�");
    htmlEntities.put("&atilde;", "�");
    htmlEntities.put("&Atilde;", "�");
    htmlEntities.put("&aacute;", "�");
    htmlEntities.put("&Aacute;", "�");
    htmlEntities.put("&acirc;", "�");
    htmlEntities.put("&auml;", "�");
    htmlEntities.put("&Auml;", "�");
    htmlEntities.put("&Acirc;", "�");
    htmlEntities.put("&aring;", "�");
    htmlEntities.put("&Aring;", "�");
    htmlEntities.put("&aelig;", "�");
    htmlEntities.put("&AElig;", "�");
    htmlEntities.put("&ccedil;", "�");
    htmlEntities.put("&Ccedil;", "�");
    htmlEntities.put("&eacute;", "�");
    htmlEntities.put("&Eacute;", "�");
    htmlEntities.put("&egrave;", "�");
    htmlEntities.put("&Egrave;", "�");
    htmlEntities.put("&ecirc;", "�");
    htmlEntities.put("&Ecirc;", "�");
    htmlEntities.put("&euml;", "�");
    htmlEntities.put("&Euml;", "�");
    htmlEntities.put("&iuml;", "�");
    htmlEntities.put("&Iuml;", "�");
    htmlEntities.put("&iacute;", "�");
    htmlEntities.put("&Iacute;", "�");
    htmlEntities.put("&ocirc;", "�");
    htmlEntities.put("&Ocirc;", "�");
    htmlEntities.put("&otilde;", "�");
    htmlEntities.put("&Otilde;", "�");
    htmlEntities.put("&oacute;", "�");
    htmlEntities.put("&Oacute;", "�");
    htmlEntities.put("&uacute;", "�");
    htmlEntities.put("&Uacute;", "�");
    htmlEntities.put("&ouml;", "�");
    htmlEntities.put("&Ouml;", "�");
    htmlEntities.put("&oslash;", "�");
    htmlEntities.put("&Oslash;", "�");
    htmlEntities.put("&szlig;", "�");
    htmlEntities.put("&ugrave;", "�");
    htmlEntities.put("&Ugrave;", "�");
    htmlEntities.put("&ucirc;", "�");
    htmlEntities.put("&Ucirc;", "�");
    htmlEntities.put("&uuml;", "�");
    htmlEntities.put("&Uuml;", "�");
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
   * Converte caracteres especiais para elementos HTML. C�digo "gentilmente sugado" do site
   *
   * @param source String que ter� os elementos substitu�dos.
   * @return Texto formatado.
   */
  public static final String converterParaElementosHTMLEspeciais(String source) {
    HashMap<Character, String> htmlEntities;
    htmlEntities = new HashMap<Character, String>();
    htmlEntities.put('<', "&lt;");
    htmlEntities.put('>', "&gt;");
    htmlEntities.put('&', "&amp;");
    htmlEntities.put('\\', "&quot;");
    htmlEntities.put('�', "&agrave;");
    htmlEntities.put('�', "&Agrave;");
    htmlEntities.put('�', "&atilde;");
    htmlEntities.put('�', "&Atilde;");
    htmlEntities.put('�', "&aacute;");
    htmlEntities.put('�', "&Aacute;");
    htmlEntities.put('�', "&acirc;");
    htmlEntities.put('�', "&auml;");
    htmlEntities.put('�', "&Auml;");
    htmlEntities.put('�', "&Acirc;");
    htmlEntities.put('�', "&aring;");
    htmlEntities.put('�', "&Aring;");
    htmlEntities.put('�', "&aelig;");
    htmlEntities.put('�', "&AElig;");
    htmlEntities.put('�', "&ccedil;");
    htmlEntities.put('�', "&Ccedil;");
    htmlEntities.put('�', "&eacute;");
    htmlEntities.put('�', "&Eacute;");
    htmlEntities.put('�', "&egrave;");
    htmlEntities.put('�', "&Egrave;");
    htmlEntities.put('�', "&ecirc;");
    htmlEntities.put('�', "&Ecirc;");
    htmlEntities.put('�', "&euml;");
    htmlEntities.put('�', "&Euml;");
    htmlEntities.put('�', "&iuml;");
    htmlEntities.put('�', "&Iuml;");
    htmlEntities.put('�', "&iacute;");
    htmlEntities.put('�', "&Iacute;");
    htmlEntities.put('�', "&ocirc;");
    htmlEntities.put('�', "&Ocirc;");
    htmlEntities.put('�', "&otilde;");
    htmlEntities.put('�', "&Otilde;");
    htmlEntities.put('�', "&oacute;");
    htmlEntities.put('�', "&Oacute;");
    htmlEntities.put('�', "&uacute;");
    htmlEntities.put('�', "&Uacute;");
    htmlEntities.put('�', "&ouml;");
    htmlEntities.put('�', "&Ouml;");
    htmlEntities.put('�', "&oslash;");
    htmlEntities.put('�', "&Oslash;");
    htmlEntities.put('�', "&szlig;");
    htmlEntities.put('�', "&ugrave;");
    htmlEntities.put('�', "&Ugrave;");
    htmlEntities.put('�', "&ucirc;");
    htmlEntities.put('�', "&Ucirc;");
    htmlEntities.put('�', "&uuml;");
    htmlEntities.put('�', "&Uuml;");
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