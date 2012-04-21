package com.rbardini.carteiro;

import java.io.Serializable;
import java.util.Date;

import org.alfredlibrary.utilitarios.correios.RegistroRastreamento;

public class PostalRecord implements Serializable {
  private static final long serialVersionUID = 1L;

  private String cod;
  private int pos;
  private Date date;
  private String loc;
  private String info;
  private String status;

  public PostalRecord() {}

  public PostalRecord(String cod, int pos) {
    this.cod = cod;
    this.pos = pos;
  }

  public PostalRecord(String cod, int pos, RegistroRastreamento rr) {
    this.cod = cod;
    this.pos = pos;
    this.date = rr.getDataHora();
    this.loc = rr.getLocal();
    this.info = rr.getDetalhe();
    this.status = rr.getAcao();
  }

  public PostalRecord(String cod, int pos, Date date, String status, String loc, String info) {
    this.cod = cod;
    this.pos = pos;
    this.date = date;
    this.status = status;
    this.loc = loc;
    this.info = info;
  }

  public String getCod() { return cod; }
  public void setCod(String cod) { this.cod = cod; }

  public int getPos() { return pos; }
  public void setPos(int pos) { this.pos = pos; }

  public Date getDate() { return date; }
  public void setDate(Date date) { this.date = date; }

  public String getLoc() { return loc; }
  public void setLoc(String loc) { this.loc = loc; }

  public String getInfo() { return info; }
  public String getSafeInfo() { return (info != null) ? info : status; }
  public void setInfo(String info) { this.info = info; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public RegistroRastreamento getReg() {
    RegistroRastreamento rr = new RegistroRastreamento();
    rr.setDataHora(date);
    rr.setLocal(loc);
    rr.setDetalhe(info);
    rr.setAcao(status);
    return rr;
  }
  public void setReg(RegistroRastreamento rr) {
    this.date = rr.getDataHora();
    this.loc = rr.getLocal();
    this.info = rr.getDetalhe();
    this.status = rr.getAcao();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PostalRecord) {
      PostalRecord pr = (PostalRecord) obj;
      return (cod == null ? pr.getCod() == null : cod.equals(pr.getCod())) &&
           (date == null ? pr.getDate() == null : date.equals(pr.getDate())) &&
           (loc == null ? pr.getLoc() == null : loc.equals(pr.getLoc())) &&
           (info == null ? pr.getInfo() == null : info.equals(pr.getInfo())) &&
           (status == null ? pr.getStatus() == null : status.equals(pr.getStatus()));
    }
    return false;
  }
}
