package com.rbardini.carteiro.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;
import org.alfredlibrary.utilitarios.correios.RegistroRastreamento;
import android.content.Context;
import com.rbardini.carteiro.util.PostalUtils;

public class PostalItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private String cod;
  private String desc;
  private Date date;
  private String loc;
  private String info;
  private String status;
  private boolean fav;
  private boolean archived;

  public PostalItem() {}

  public PostalItem(String cod) {
    this.cod = cod;
    this.desc = null;
    this.fav = false;
    this.archived = false;
  }

  public PostalItem(String cod, String desc) {
    this.cod = cod;
    this.desc = desc;
    this.fav = false;
    this.archived = false;
  }

  public PostalItem(String cod, boolean fav) {
    this.cod = cod;
    this.desc = null;
    this.fav = fav;
    this.archived = false;
  }

  public PostalItem(String cod, String desc, boolean fav) {
    this.cod = cod;
    this.desc = desc;
    this.fav = fav;
    this.archived = false;
  }

  public PostalItem(String cod, String desc, RegistroRastreamento rr, boolean fav) {
    this.cod = cod;
    this.desc = desc;
    this.date = rr.getDataHora();
    this.loc = rr.getLocal();
    this.info = rr.getDetalhe();
    this.status = rr.getAcao();
    this.fav = fav;
    this.archived = false;
  }

  public PostalItem(String cod, String desc, Date date, String loc, String info, String status, boolean fav, boolean archived) {
    this.cod = cod;
    this.desc = desc;
    this.date = date;
    this.loc = loc;
    this.info = info;
    this.status = status;
    this.fav = fav;
    this.archived = archived;
  }

  public String getCod() { return cod; }
  public void setCod(String cod) { this.cod = cod; }

  public String getDesc() { return desc; }
  public String getSafeDesc() { return (desc != null) ? desc : cod; }
  public String getFullDesc() { return (desc != null) ? desc + " (" + cod + ")" : cod; }
  public void setDesc(String desc) { this.desc = desc; }

  public Date getDate() { return date; }
  public void setDate(Date date) { this.date = date; }

  public String getLoc() { return loc; }
  public void setLoc(String loc) { this.loc = loc; }

  public String getInfo() { return info; }
  public String getSafeInfo() { return (info != null) ? info : status; }
  public String getFullInfo() { return status + ((info != null) ? ". " + info : ""); }
  public void setInfo(String info) { this.info = info; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public boolean isFav() { return fav; }
  public void setFav(boolean fav) { this.fav = fav; }
  public boolean toggleFav() { return this.fav = !this.fav; }

  public boolean isArchived() { return archived; }
  public void setArchived(boolean archived) { this.archived = archived; }
  public boolean toggleArchived() { return this.archived = !this.archived; }

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

  public String getService() {
    return PostalUtils.Service.getService(cod);
  }

  public int getFlag(Context context) {
    String resourceName = "flag_" + cod.substring(11, 13).toLowerCase(Locale.getDefault());
    return context.getResources().getIdentifier(resourceName, "drawable", context.getApplicationInfo().packageName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PostalItem) {
      PostalItem pi = (PostalItem) obj;
      return cod == null ? pi.getCod() == null : cod.equals(pi.getCod());
    }
    return false;
  }
}
