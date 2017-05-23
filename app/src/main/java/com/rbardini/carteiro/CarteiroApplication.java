package com.rbardini.carteiro;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatDelegate;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncService;

import java.util.HashSet;
import java.util.Set;

public class CarteiroApplication extends Application {
  public static boolean syncing = false;

  private Tracker tracker;
  private Set<String> updatedCods;
  private boolean updatedList;

  @Override
  public void onCreate() {
    super.onCreate();

    LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        syncing = intent.getBooleanExtra(SyncService.EXTRA_RUNNING, false);
      }
    }, new IntentFilter(SyncService.ACTION_SYNC));

    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    tracker = analytics.newTracker(R.xml.tracker_config);
    tracker.enableAdvertisingIdCollection(true);

    updatedCods = new HashSet<>();
    updatedList = false;

    setTheme();
    scheduleSync();
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

  public void setTheme() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String lightTheme = getString(R.string.theme_light);
    String darkTheme = getString(R.string.theme_dark);
    String currentTheme = prefs.getString(getString(R.string.pref_key_theme), lightTheme);

    if (currentTheme.equals(darkTheme)) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
  }

  public void scheduleSync() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    boolean onBoot = prefs.getBoolean(getString(R.string.pref_key_on_boot), false);
    boolean autoSync = prefs.getBoolean(getString(R.string.pref_key_auto_sync), true);

    if (!onBoot && autoSync) {
      // Schedule sync service on first start
      SyncService.scheduleSync(this);
      prefs.edit().putBoolean(getString(R.string.pref_key_on_boot), true).apply();
    }
  }
}
