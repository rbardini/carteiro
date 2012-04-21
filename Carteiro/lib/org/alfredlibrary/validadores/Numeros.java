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
package org.alfredlibrary.validadores;

import java.math.BigDecimal;

/**
 * Validador de Números
 *
 * @author Marlon Silva Carvalho
 * @since 02/06/2009
 */
final public class Numeros {

  /**
   * Verificar se o numero da String eh um Numero.
   *
   * @param numero Numero.
   * @return Verdadeiro caso seja Numero. Falso, caso contrario.
   */
  public static boolean isNumber(String numero) {
    try {
      return isBigDecimal(numero);
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o numero da String eh um Short.
   *
   * @param numero Numero.
   * @return Verdadeiro caso seja Short. Falso, caso contrario.
   */
  public static boolean isShort(String numero) {
    try {
      Short.valueOf(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o numero da String eh um inteiro.
   *
   * @param numero Numero.
   * @return Verdadeiro caso seja inteiro. Falso, caso contrario.
   */
  public static boolean isInteger(String numero) {
    try {
      Long.valueOf(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o numero da String eh um Double.
   *
   * @param numero Numero.
   * @return Verdadeiro caso seja Double. Falso, caso contrario.
   */
  public static boolean isDouble(String numero) {
    try {
      Double.valueOf(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o número da String é um Float.
   *
   * @param numero Número.
   * @return Verdadeiro caso seja Float. Falso, caso contrário.
   */
  public static boolean isFloat(String numero) {
    try {
      Float.valueOf(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o número da String é um BigDecimal.
   *
   * @param numero Número.
   * @return Verdadeiro caso seja BigDecimal. Falso, caso contrário.
   */
  public static boolean isBigDecimal(String numero) {
    try {
      new BigDecimal(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

  /**
   * Verificar se o número da String  é um Long.
   *
   * @param numero Número.
   * @return Verdadeiro caso seja Long. Falso, caso contrário.
   */
  public static boolean isLong(String numero) {
    try {
      Long.valueOf(numero);
      return true;
    } catch (RuntimeException exception) {
      return false;
    }
  }

}