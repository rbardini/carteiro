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
package org.alfredlibrary.utilitarios.correios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.alfredlibrary.AlfredException;
import org.alfredlibrary.formatadores.Data;
import org.alfredlibrary.utilitarios.net.WorldWideWeb;
import org.alfredlibrary.utilitarios.texto.HTML;
import org.alfredlibrary.utilitarios.texto.Texto;

/**
 * Utilitários para obter informações de rastreamento de objetos através do site dos Correios.
 *
 * @author Rodrigo Moreira Fagundes
 * @since 19/05/2009
 */
final public class Rastreamento {

  /**
   * Rastrear um objeto a partir do seu código.
   *
   * @param codObjeto Código do Objeto.
   * @return Coleção de movimentações de rastreamento
   */
  public static List <RegistroRastreamento> rastrear(String codObjeto) {
    validarCodObjeto(codObjeto);
    String conteudo = WorldWideWeb.obterConteudoSite("http://websro.correios.com.br/sro_bin/txect01$.QueryList?P_LINGUA=001&P_TIPO=001&P_COD_UNI=" + codObjeto, "ISO-8859-1");

    BufferedReader br = new BufferedReader(new StringReader(conteudo), 8192);
    String linha = null;

    List<RegistroRastreamento> listRegistroRastreamento = new ArrayList <RegistroRastreamento>();

    if ( conteudo.indexOf("O nosso sistema não possui dados sobre o objeto") > -1 ) {
      throw new AlfredException("O sistema dos Correios não possui dados sobre o objeto informado");
    } else if ( conteudo.indexOf("O horário não indica quando a situação ocorreu") == -1 ) {
      throw new AlfredException("Não foi possível obter contato com o site dos Correios");
    }

    try {
      while( (linha = br.readLine()) != null ) {
        linha = HTML.desconverterElementosHTMLEspeciais(linha, 0);
        if ( linha.indexOf("<tr><td ") > -1 ) {
          if (linha.indexOf("rowspan=1") > -1) {
            listRegistroRastreamento.add(formarRegisto(linha));
          } else if (linha.indexOf("rowspan=2") > -1|| linha.indexOf("rowspan=3") > -1) {
            RegistroRastreamento rr = formarRegisto(linha);
            linha = br.readLine();
            rr = complementarRegistro(rr, linha);
            listRegistroRastreamento.add(rr);
          }
        }
      }
    } catch (IOException e) {
      throw new AlfredException(e);
    }
    return listRegistroRastreamento;
  }

  /**
   * Valida o formato do código de objeto informado
   *
   * @param codObjeto
   * @return boolean
   */
  private static void validarCodObjeto(String codObjeto) {
    if (codObjeto == null) {
      throw new AlfredException("Informe o Código do objeto.");
    } else if ( "".equals(codObjeto) ) {
      throw new AlfredException("Informe o Código do objeto.");
    } else if (codObjeto.length() != 13) {
      throw new AlfredException("Código de Rastreamento deve ter tamanho 13.");
    } else if (!"".equals(Texto.manterNumeros(codObjeto.substring(0, 1)))
        || !"".equals(Texto.manterNumeros(codObjeto.substring(11, 13)))
        || Texto.manterNumeros(codObjeto.substring(2, 11)).length() != 9) {
      throw new AlfredException("Código de Rastreamento fora do padrão.");
    }
  }

  /**
   * Transforma uma linha de texto em um item de rastreamento
   *
   * @param linha
   * @return Registro de Rastreamento
   */
  private static RegistroRastreamento formarRegisto(String linha) {
    int linhaIndex = linha.indexOf("rowspan=") + 10;
    RegistroRastreamento rr = new RegistroRastreamento();
    rr.setDataHora(Data.formatar(linha.substring(linhaIndex, linha.indexOf("</td><td>", linhaIndex) + 1), "dd/MM/yyyy HH:mm"));
    linhaIndex = linha.indexOf("</td><td>", linhaIndex) + 9;
    rr.setLocal(linha.substring(linhaIndex, linha.indexOf("</td><td>", linhaIndex)).replaceAll("\\s+", " "));
    linhaIndex = linha.indexOf("</td><td>", linhaIndex) + 30;
    rr.setAcao(linha.substring(linhaIndex, linha.indexOf("</font></td></tr>", linhaIndex)));
    if (rr.getAcao().equals(" ")) { rr.setAcao("Indeterminado"); }
    return rr;
  }

  /**
   * Complementa o item de rastreamento com valores da linha de texto auxiliar
   *
   * @param rr Registro a ser complementado
   * @param linha Linha de texto auxiliar com padrão diferente da que origina um Registro
   * @return Registro de Rastreamento Registro atualizado
   */
  private static RegistroRastreamento complementarRegistro(RegistroRastreamento rr, String linha) {
    int linhaIndex = linha.indexOf("colspan=") + 10;
    rr.setDetalhe(linha.substring(linhaIndex, linha.indexOf("</td></tr>", linhaIndex)).replaceAll("\\s+", " "));
    if (rr.getDetalhe().startsWith("Por favor, entre em contato")) { rr.setDetalhe("Entre em contato com os Correios."); }
    return rr;
  }


}
