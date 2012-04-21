package com.rbardini.carteiro;

import java.util.HashSet;
import java.util.Set;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.ui.PreferencesActivity.Preferences;

public class CarteiroApplication extends Application {
  public static State state;

  private Set<String> updatedCods;
  private boolean updatedList;

  @Override
  public void onCreate() {
    super.onCreate();

    state = new State();
    updatedCods = new HashSet<String>();

    // Schedule sync service on first start
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(Preferences.ON_BOOT, false) && prefs.getBoolean(Preferences.AUTO_SYNC, true)) {
          SyncService.scheduleSync(this);
          prefs.edit().putBoolean(Preferences.ON_BOOT, true).commit();
        }
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
