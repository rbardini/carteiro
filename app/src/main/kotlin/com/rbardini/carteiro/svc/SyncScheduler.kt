package com.rbardini.carteiro.svc

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import com.rbardini.carteiro.R

object SyncScheduler {
  private const val TAG = "SyncScheduler"
  private const val JOB_ID = 1
  private const val SYNC_INTERVAL_MILLIS = 15 * 60 * 1000L
  private const val SYNC_FLEX_MILLIS = 5 * 60 * 1000L

  @JvmStatic
  fun schedule(context: Context) {
    val component = ComponentName(context, SyncService::class.java)
    val builder = JobInfo.Builder(JOB_ID, component)
      .setRequiredNetworkType(getNetworkType(context))
      .setPersisted(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      builder.setPeriodic(SYNC_INTERVAL_MILLIS, SYNC_FLEX_MILLIS)
    } else {
      builder.setPeriodic(SYNC_INTERVAL_MILLIS)
    }

    getScheduler(context).schedule(builder.build())
    Log.i(TAG, "Sync scheduled")
  }

  @JvmStatic
  fun unschedule(context: Context) {
    getScheduler(context).cancel(JOB_ID)
    Log.i(TAG, "Sync unscheduled")
  }

  private fun getScheduler(context: Context): JobScheduler =
    context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

  private fun getNetworkType(context: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    val key = context.getString(R.string.pref_key_sync_wifi_only)

    return if (prefs.getBoolean(key, false)) JobInfo.NETWORK_TYPE_UNMETERED else JobInfo.NETWORK_TYPE_ANY
  }
}
