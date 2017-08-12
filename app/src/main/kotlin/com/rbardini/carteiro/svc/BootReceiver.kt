package com.rbardini.carteiro.svc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import com.rbardini.carteiro.R

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    if (prefs.getBoolean(context.getString(R.string.pref_key_auto_sync), true)) {
      SyncScheduler.schedule(context)
      prefs.edit().putBoolean(context.getString(R.string.pref_key_on_boot), true).apply()
    }
  }
}
