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

import java.util.Date;

/**
 * Bean que representa um Registro de Rastreamento de objeto nos correios.
 *
 * @author Rodrigo Moreira Fagundes
 * @since 19/05/2010
 */
public class RegistroRastreamento {

  private Date dataHora;
  private String local;
  private String acao;
  private String detalhe;

  public Date getDataHora() {
    return dataHora;
  }
  public void setDataHora(Date dataHora) {
    this.dataHora = dataHora;
  }
  public String getLocal() {
    return local;
  }
  public void setLocal(String local) {
    this.local = local;
  }
  public String getAcao() {
    return acao;
  }
  public void setAcao(String acao) {
    this.acao = acao;
  }
  public String getDetalhe() {
    return detalhe;
  }
  public void setDetalhe(String detalhe) {
    this.detalhe = detalhe;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof RegistroRastreamento) {
      RegistroRastreamento rr = (RegistroRastreamento) obj;
      return (dataHora == null ? rr.getDataHora() == null : dataHora.equals(rr.getDataHora())) &&
           (local == null ? rr.getLocal() == null : local.equalsIgnoreCase(rr.getLocal())) &&
           (acao == null ? rr.getAcao() == null : acao.equalsIgnoreCase(rr.getAcao())) &&
           (detalhe == null ? rr.getDetalhe() == null : detalhe.equalsIgnoreCase(rr.getDetalhe()));
    }
    return false;
  }
}
