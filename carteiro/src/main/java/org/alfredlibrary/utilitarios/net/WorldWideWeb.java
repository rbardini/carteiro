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
package org.alfredlibrary.utilitarios.net;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import org.alfredlibrary.AlfredException;
import org.alfredlibrary.ProxyConfig;

/**
 * Utilitários para WWW.
 *
 * @author Marlon Silva Carvalho
 * @since 02/06/2009
 */
final public class WorldWideWeb {
  private static ProxyConfig proxy = null;

  public static void setProxyConfig(ProxyConfig proxyConfig) {
    proxy = proxyConfig;
  }

  public static InputStream obterConteudoArquivo(String u) {
    URL url;
    try {
      url = new URL(u);
      URLConnection conn = null;
      if ( proxy != null )
        conn = url.openConnection(proxy.getProxy());
      else conn = url.openConnection();

        return new DataInputStream(conn.getInputStream());
    } catch (MalformedURLException e) {
      throw new AlfredException(e);
    } catch (IOException e) {
      throw new AlfredException(e);
    }
  }

  /**
   * Obter o conteúdo de um site.
   *
   * @param u URL do Site.
   * @return String contendo todo o conteúdo do site em HTML.
   * @deprecated
   */
  @Deprecated
  public static String obterConteudoSite(String u) {
        URL url;
    try {
      url = new URL(u);
      URLConnection conn = null;
      if ( proxy != null )
        conn = url.openConnection(proxy.getProxy());
      else conn = url.openConnection();
          conn.setDoOutput(true);
          BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),Charset.forName("UTF-8")));
          String line;
          StringBuilder resultado = new StringBuilder();
          while ((line = rd.readLine()) != null) {
              resultado.append(line);
              resultado.append("\n");
          }
          rd.close();
          return resultado.toString();
    } catch (MalformedURLException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    } catch (IOException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    }
  }

  /**
   * Obter o conteúdo de um site.
   *
   * @param u URL do Site.
   * @return String contendo todo o conteúdo do site em HTML.
   */
  public static String obterConteudoSite(String u, String characterSet) {
        URL url;
    try {
      url = new URL(u);
      URLConnection conn = null;
      if ( proxy != null )
        conn = url.openConnection(proxy.getProxy());
      else conn = url.openConnection();
          conn.setDoOutput(true);
          BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),Charset.forName(characterSet)), 8192);
          String line;
          StringBuilder resultado = new StringBuilder();
          while ((line = rd.readLine()) != null) {
              resultado.append(line);
              resultado.append("\n");
          }
          rd.close();
          return resultado.toString();
    } catch (MalformedURLException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    } catch (IOException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    }
  }

  public static String obterConteudoSite(String u, String encode, Map<String,String> parametros) {
        URL url;
    try {
      StringBuilder strParams = new StringBuilder();
      if ( parametros != null ) {
        for(String chave:parametros.keySet()) {
          strParams.append(URLEncoder.encode(chave,"UTF-8"));
          strParams.append("=");
          strParams.append(URLEncoder.encode(parametros.get(chave), encode));
          strParams.append("&");
        }
      }
      url = new URL(u);
      URLConnection conn = null;
      if ( proxy != null )
        conn = url.openConnection(proxy.getProxy());
      else conn = url.openConnection();
          conn.setDoOutput(true);
          OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
          wr.write(strParams.toString());
          wr.flush();
          BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          String line;
          StringBuilder resultado = new StringBuilder();
          while ((line = rd.readLine()) != null) {
              resultado.append(line);
          }
          wr.close();
          rd.close();
          return resultado.toString();
    } catch (MalformedURLException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    } catch (IOException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    }
  }

  /**
   * Obter o conteúdo de um site usando o método POST.
   *
   * @param u URL do Site.
   * @param parametros Parâmetros que serão usandos na requisição ao site.
   * @return String contendo todo o conteúdo do site em HTML.
   */
  public static String obterConteudoSite(String u, Map<String,String> parametros) {
    return obterConteudoSite(u,parametros,null);
  }

  public static String obterConteudoSite(String u, Map<String,String> parametros, Map<String,String> headers) {
        URL url;
    try {
      StringBuilder strParams = new StringBuilder();
      if ( parametros != null ) {
        for(String chave:parametros.keySet()) {
          strParams.append(URLEncoder.encode(chave,"UTF-8"));
          strParams.append("=");
          strParams.append(URLEncoder.encode(parametros.get(chave),"UTF-8"));
          strParams.append("&");
        }
      }
      url = new URL(u);
      URLConnection conn = null;

      if ( proxy != null )
        conn = url.openConnection(proxy.getProxy());
      else conn = url.openConnection();

      if ( headers != null ) {
        for(String header:headers.keySet()) {
          conn.setRequestProperty(header, headers.get(header));
        }
      }
          conn.setDoOutput(true);
          OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
          wr.write(strParams.toString());
          wr.flush();
          BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
          String line;
          StringBuilder resultado = new StringBuilder();
          while ((line = rd.readLine()) != null) {
              resultado.append(line);
          }
          wr.close();
          rd.close();
          return resultado.toString();
    } catch (MalformedURLException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    } catch (IOException e) {
      throw new AlfredException("Não foi possível obter contato com o site " + u, e);
    }
  }
}