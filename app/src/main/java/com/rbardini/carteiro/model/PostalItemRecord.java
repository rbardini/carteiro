package com.rbardini.carteiro.model;

import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.util.Tracker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostalItemRecord implements Serializable {
  private static final long serialVersionUID = 1L;

  private PostalItem pi;
  private List<PostalRecord> prList;

  public PostalItemRecord(String cod) {
    this.pi = new PostalItem(cod);
    this.prList = new ArrayList<>();
  }

  public PostalItemRecord(PostalItem pi) {
    this.pi = pi;
    this.prList = new ArrayList<>();
  }

  public PostalItemRecord(PostalItem pi, PostalRecord pr) {
    this.pi = pi;
    this.prList = new ArrayList<>();
    this.prList.add(pr);
  }

  public PostalItemRecord(PostalItem pi, List<PostalRecord> prList) {
    this.pi = pi;
    this.prList = prList;
  }

  public PostalItem getPostalItem() { return this.pi; }
  public void setPostalItem(PostalItem pi) { this.pi = pi; }

  public List<PostalRecord> getPostalRecords() { return this.prList; }
  public void setPostalRecords(List<PostalRecord> prList) { this.prList = prList; }

  public PostalRecord getPostalRecord(int pos) { return this.prList.get(pos); }
  public void setPostalRecord(PostalRecord pr) { this.prList.add(pr); }
  public void setPostalRecord(PostalRecord pr, int pos) { this.prList.add(pos, pr); }

  public PostalRecord getLastPostalRecord() {
    int size = this.prList.size();
    return size > 0 ? this.prList.get(size - 1) : null;
  }

  // Expose PostalItem getters
  public String getCod() { return this.pi.getCod(); }
  public String getDesc() { return this.pi.getDesc(); }
  public String getSafeDesc() { return this.pi.getSafeDesc(); }
  public String getFullDesc() { return this.pi.getFullDesc(); }
  public Date getDate() { return this.pi.getDate(); }
  public String getLoc() { return this.pi.getLoc(); }
  public String getInfo() { return this.pi.getInfo(); }
  public String getSafeInfo() { return this.pi.getSafeInfo(); }
  public String getFullInfo() { return this.pi.getFullInfo(); }
  public String getStatus() { return this.pi.getStatus(); }
  public boolean isFav() { return this.pi.isFav(); }

  public PostalItemRecord fetch() throws Exception {
    this.prList = Tracker.track(this.pi.getCod());
    return this;
  }

  public PostalItemRecord loadFrom(DatabaseHelper dh) {
    this.pi = dh.getPostalItem(this.getCod());
    dh.getPostalRecords(this.prList, this.getCod());

    return this;
  }

  public PostalItemRecord saveTo(DatabaseHelper dh) {
    dh.beginTransaction();

    dh.insertPostalItem(this.pi);
    dh.insertPostalRecords(prList);

    dh.setTransactionSuccessful();
    dh.endTransaction();

    return this;
  }

  public PostalItemRecord archiveTo(DatabaseHelper dh) {
    dh.beginTransaction();

    dh.archivePostalItem(this.pi.getCod());

    dh.setTransactionSuccessful();
    dh.endTransaction();

    return this;
  }

  public PostalItemRecord deleteFrom(DatabaseHelper dh) {
    dh.beginTransaction();

    dh.deletePostalItem(this.pi.getCod());

    dh.setTransactionSuccessful();
    dh.endTransaction();

    return this;
  }

  public int size() {
    return this.prList.size();
  }

  public boolean isEmpty() {
    return this.prList.isEmpty();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof PostalItemRecord) {
      PostalItemRecord pir = (PostalItemRecord) obj;

      if (pir.getPostalItem().equals(this.getPostalItem())) {
        if (pir.size() == this.size()) {
          for (int i = 0; i < this.size(); i++) {
            if (!pir.getPostalRecord(i).equals(this.getPostalRecord(i))) {
              return false;
            }
          }
          return true;
        }
      }
    }
    return false;
  }
}
