package com.rbardini.carteiro.svc

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.rbardini.carteiro.CarteiroApplication
import com.rbardini.carteiro.R
import com.rbardini.carteiro.model.Shipment
import com.rbardini.carteiro.util.PostalUtils

class SyncService : JobService() {
  companion object {
    const val TAG = "SyncService"
    const val EXTRA_PARAMS = "$TAG.EXTRA_PARAMS"
  }

  private val mReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = onSyncStatusChange(intent)
  }

  private lateinit var mSyncTask: AsyncTask<Shipment?, Void, Boolean>

  override fun onStartJob(params: JobParameters?): Boolean {
    Log.i(TAG, "Sync job started")

    val shipments = getShipmentsForSync()
    if (shipments.isEmpty()) return false

    registerReceiver()
    mSyncTask = SyncTask.run(application, shipments, Intent(Intent.ACTION_SYNC).putExtra(EXTRA_PARAMS, params))

    return true
  }

  override fun onStopJob(params: JobParameters?): Boolean {
    Log.i(TAG, "Sync job stopped")

    unregisterReceiver()
    mSyncTask.cancel(true)

    return false
  }

  private fun onSyncStatusChange(intent: Intent) {
    val status = intent.getIntExtra(SyncTask.EXTRA_STATUS, SyncTask.STATUS_FINISHED)
    val params = intent.getParcelableExtra<JobParameters>(EXTRA_PARAMS) ?: return

    when (status) {
      SyncTask.STATUS_FINISHED, SyncTask.STATUS_SKIPPED, SyncTask.STATUS_ERROR -> {
        unregisterReceiver()
        jobFinished(params, false)

        Log.i(TAG, "Sync job finished")
      }
    }
  }

  private fun registerReceiver() = LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, IntentFilter(SyncTask.ACTION_SYNC))

  private fun unregisterReceiver() = LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)

  private fun getShipmentsForSync(): List<Shipment> {
    val app = application as CarteiroApplication
    val flags = getSyncFlags()

    return app.databaseHelper.getShallowShipmentsForSync(flags)
  }

  private fun getSyncFlags(): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    var flags = 0

    if (prefs.getBoolean(getString(R.string.pref_key_sync_favorites_only), false)) {
      flags = flags or PostalUtils.Category.FAVORITES
    }
    if (prefs.getBoolean(getString(R.string.pref_key_dont_sync_delivered_items), false)) {
      flags = flags or PostalUtils.Category.UNDELIVERED
    }

    return flags
  }
}
