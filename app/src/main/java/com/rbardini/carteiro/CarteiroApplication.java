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
import android.util.Log;
import android.webkit.WebView;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.svc.SyncTask;

public class CarteiroApplication extends Application {
  private static final String TAG = "CarteiroApplication";

  public static boolean syncing = false;

  private Tracker tracker;

  @Override
  public void onCreate() {
    super.onCreate();

    LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        syncing = intent.getBooleanExtra(SyncTask.EXTRA_RUNNING, false);
      }
    }, new IntentFilter(SyncTask.ACTION_SYNC));

    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    tracker = analytics.newTracker(R.xml.tracker_config);
    tracker.enableAdvertisingIdCollection(true);

    setTheme();
    scheduleSync();
  }

  public Tracker getTracker() {
    return tracker;
  }

  public DatabaseHelper getDatabaseHelper() {
    return DatabaseHelper.getInstance(this);
  }

  private void setTheme() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String lightTheme = getString(R.string.theme_light);
    String darkTheme = getString(R.string.theme_dark);
    String currentTheme = prefs.getString(getString(R.string.pref_key_theme), lightTheme);

    if (currentTheme.equals(darkTheme)) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

      // Workaround for broken colors after WebView is created (https://stackoverflow.com/q/44035654)
      try {
        new WebView(getApplicationContext());
      } catch (Exception e) {
        Log.w(TAG, "Could not instantiate WebView to avoid night mode issues");
      }
    }
  }

  private void scheduleSync() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    boolean onBoot = prefs.getBoolean(getString(R.string.pref_key_on_boot), false);
    boolean autoSync = prefs.getBoolean(getString(R.string.pref_key_auto_sync), true);

    if (!onBoot && autoSync) {
      // Schedule sync on first start
      SyncScheduler.schedule(this);
      prefs.edit().putBoolean(getString(R.string.pref_key_on_boot), true).apply();
    }
  }
}
