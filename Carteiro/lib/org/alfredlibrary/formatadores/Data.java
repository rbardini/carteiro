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
package org.alfredlibrary.formatadores;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.alfredlibrary.AlfredException;

/**
 * Utilitário para formatar Datas.
 *
 * @author Rodrigo Moreira Fagundes
 * @author Marlon Silva Carvalho
 * @since 14/04/2010
 */
public final class Data {

  private Data() { }

  /**
   * Obtém uma String correspondente a uma determinada data,
   * de acordo com o formato especificado.
   *
   * @param data Data.
   * @param formato Formato do texto de saída.
   * @return String Texto com a data no formato especificado.
   */
  public static String formatar (Date data, String formato) {
    if (data != null) {
      DateFormat dateFormat = new SimpleDateFormat(formato);
      return dateFormat.format(data);
    } else {
      return "";
    }
  }

  /**
   * Obtém uma Data a partir de um texto passado no formato especificado.
   *
   * @param strData Texto contendo a data.
   * @param formato Formato do texto passado em "strData".
   * @return Date Data.
   */
  public static Date formatar (String strData, String formato) {
    if (strData != null) {
      DateFormat df = new SimpleDateFormat(formato);
      try {
        return df.parse(strData);
      } catch (ParseException e) {
        throw new AlfredException(e);
      }
    }
    return null;
  }

  /**
   * Formatar uma data no formato dd de MM de aaaa.
   * Exemplo: 21 de Janeiro de 2009
   *
   * @param data Data a ser formatada.
   * @return Data formatada.
   */
  public static String formatarPorExtenso(Date data) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(data);
    String mes = new DateFormatSymbols(Locale.getDefault()).getMonths()[cal.get(Calendar.MONTH)];
    String dia = (cal.get(Calendar.DAY_OF_MONTH) < 10)  ?  "0" + cal.get(Calendar.DAY_OF_MONTH) : String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    return new StringBuilder().append(dia).append(" de ").append(mes).append(" de ").append(cal.get(Calendar.YEAR)).toString();
  }

}