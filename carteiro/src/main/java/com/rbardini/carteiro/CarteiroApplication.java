package com.rbardini.carteiro;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;

import java.util.HashSet;
import java.util.Set;

public class CarteiroApplication extends Application {
  public static State state;

  private Tracker tracker;
  private Set<String> updatedCods;
  private boolean updatedList;

  @Override
  public void onCreate() {
    super.onCreate();

    state = new State();

    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    tracker = analytics.newTracker(R.xml.tracker_config);
    tracker.enableAdvertisingIdCollection(true);

    updatedCods = new HashSet<>();
    updatedList = false;

    // Schedule sync service on first start
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (!prefs.getBoolean(getString(R.string.pref_key_on_boot), false) && prefs.getBoolean(getString(R.string.pref_key_auto_sync), true)) {
      SyncService.scheduleSync(this);
      prefs.edit().putBoolean(getString(R.string.pref_key_on_boot), true).apply();
    }
  }

  public Tracker getTracker() {
    return tracker;
  }

  public DatabaseHelper getDatabaseHelper() {
    return DatabaseHelper.getInstance(this);
  }

  public boolean addUpdatedCod(String cod) {
    return updatedCods.add(cod);
  }

  public Set<String> getUpdatedCods() {
    return updatedCods;
  }

  public boolean isUpdatedCod(String cod) {
    return updatedCods.contains(cod);
  }

  public void setUpdatedList() {
    updatedList = true;
  }

  public boolean hasUpdate() {
    return updatedList || !updatedCods.isEmpty();
  }

  public void clearUpdate() {
    updatedList = false;
    updatedCods.clear();
  }

  public static class State {
    public DetachableResultReceiver receiver;
    public boolean syncing = false;

    private State() {
      receiver = new DetachableResultReceiver(new Handler());
    }
  }
}
