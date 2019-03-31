package com.rbardini.carteiro;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.svc.SyncTask;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.fabric.sdk.android.Fabric;

public class CarteiroApplication extends Application {
  private static final String TAG = "CarteiroApplication";

  public static boolean syncing = false;

  @Override
  public void onCreate() {
    super.onCreate();

    // Set up Crashlytics, disabled for debug builds
    Crashlytics crashlytics = new Crashlytics.Builder()
      .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
      .build();

    // Initialize Fabric with the debug-disabled Crashlytics.
    Fabric.with(this, crashlytics);

    LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        syncing = intent.getBooleanExtra(SyncTask.EXTRA_RUNNING, false);
      }
    }, new IntentFilter(SyncTask.ACTION_SYNC));

    SyncScheduler.reschedule(this);
    setupTheme();
  }

  public DatabaseHelper getDatabaseHelper() {
    return DatabaseHelper.getInstance(this);
  }

  private void setupTheme() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String lightTheme = getString(R.string.theme_light);
    String currentTheme = prefs.getString(getString(R.string.pref_key_theme), lightTheme);

    if (currentTheme.equals(lightTheme)) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

      // Workaround for broken colors after WebView is created (https://stackoverflow.com/q/44035654)
      try {
        new WebView(getApplicationContext());
      } catch (Exception e) {
        Log.w(TAG, "Could not instantiate WebView to avoid night mode issues");
      }
    }
  }
}
