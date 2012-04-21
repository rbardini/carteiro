package com.rbardini.carteiro.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.rbardini.carteiro.ui.PreferencesActivity.Preferences;

public class AlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    // Ignore broadcasts from other application installs
    if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) &&
      !intent.getData().getSchemeSpecificPart().equals(context.getPackageName())) { return; }

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    if (prefs.getBoolean(Preferences.AUTO_SYNC, true)) {
      SyncService.scheduleSync(context);
      prefs.edit().putBoolean(Preferences.ON_BOOT, true).commit();
    }
  }

}
