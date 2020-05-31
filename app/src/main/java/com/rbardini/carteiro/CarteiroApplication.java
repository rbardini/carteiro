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

import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.svc.SyncTask;
import com.rbardini.carteiro.util.UIUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class CarteiroApplication extends Application {
  private static final String TAG = "CarteiroApplication";

  public static boolean syncing = false;

  @Override
  public void onCreate() {
    super.onCreate();

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
    String theme = prefs.getString(getString(R.string.pref_key_theme), getString(R.string.theme_system));

    UIUtils.setTheme(this, theme);

    // Workaround for broken colors after WebView is created (https://stackoverflow.com/q/44035654)
    try {
      new WebView(getApplicationContext());
    } catch (Exception e) {
      Log.w(TAG, "Could not instantiate WebView to avoid night mode issues");
    }
  }
}
