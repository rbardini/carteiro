package com.rbardini.carteiro.svc

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.rbardini.carteiro.CarteiroApplication
import com.rbardini.carteiro.R
import com.rbardini.carteiro.model.Shipment
import com.rbardini.carteiro.model.ShipmentRecord
import com.rbardini.carteiro.util.MobileTracker
import com.rbardini.carteiro.util.NotificationUtils
import java.util.concurrent.TimeUnit

class SyncTask(private val app: Application, private val intent: Intent?) : AsyncTask<Shipment, Void, Boolean>() {
  companion object {
    const val TAG = "SyncTask"

    const val ACTION_SYNC = "$TAG.ACTION_SYNC"
    const val EXTRA_STATUS = "$TAG.EXTRA_STATUS"
    const val EXTRA_RUNNING = "$TAG.EXTRA_RUNNING"
    const val EXTRA_ERROR = "$TAG.EXTRA_ERROR"

    const val STATUS_RUNNING = 1
    const val STATUS_ERROR = 2
    const val STATUS_FINISHED = 3
    const val STATUS_SKIPPED = 4

    @JvmStatic fun run(app: Application, shipments: List<Shipment>, intent: Intent?) = SyncTask(app, intent).execute(*shipments.toTypedArray())
    @JvmStatic fun run(app: Application, shipments: List<Shipment>) = run(app, shipments, null)
    @JvmStatic fun run(app: Application, shipment: Shipment) = run(app, mutableListOf(shipment))
  }

  private val broadcaster = LocalBroadcastManager.getInstance(app)

  override fun doInBackground(vararg params: Shipment): Boolean {
    if (CarteiroApplication.syncing) {
      Log.i(TAG, "Sync already running")
      broadcaster.sendBroadcast(buildStatusIntent(STATUS_SKIPPED).putExtra(EXTRA_RUNNING, true))
      return false
    }

    if (!isConnected()) {
      Log.i(TAG, "Sync not possible")
      broadcaster.sendBroadcast(buildStatusIntent(STATUS_ERROR).putExtra(EXTRA_ERROR, "No network connection"))
      return false
    }

    val shipments = params.asList()
    if (shipments.isEmpty()) {
      Log.i(TAG, "No items to sync")
      broadcaster.sendBroadcast(buildStatusIntent(STATUS_SKIPPED))
      return false
    }

    val startTime = System.nanoTime()
    Log.i(TAG, "Sync started")

    broadcaster.sendBroadcast(buildStatusIntent(STATUS_RUNNING).putExtra(EXTRA_RUNNING, true))

    var hasError = false

    val oldRecordsMap = hashMapOf<String, MutableList<ShipmentRecord>>()
    shipments.forEach { oldRecordsMap.put(it.number, it.records.toMutableList()) }

    NotificationUtils.notifyOngoingSyncIfAllowed(app)

    try {
      Log.i(TAG, "Shallow-syncing ${shipments.size} item(s)...")
      MobileTracker.shallowTrack(shipments, app)

      val updatedShipments = shipments.filter {
        if (it.isEmpty()) false else !oldRecordsMap[it.number]!!.contains(it.getLastRecord())
      }

      if (updatedShipments.isNotEmpty()) {
        Log.i(TAG, "${updatedShipments.size} update(s) found, deep-syncing item(s)...")
        MobileTracker.deepTrack(updatedShipments, app)

        val fullyUpdatedShipments = updatedShipments.filter {
          if (it.isEmpty()) Log.w(TAG, "Dropped empty deep-synced item ${it.number}")
          it.isNotEmpty()
        }

        if (fullyUpdatedShipments.isNotEmpty()) {
          updateShipments(fullyUpdatedShipments)
          NotificationUtils.notifyShipmentUpdatesIfAllowed(app, fullyUpdatedShipments)
        }
      }

    } catch (e: Exception) {
      Log.e(TAG, e.message)
      hasError = true
    }

    NotificationUtils.cancelOngoingSyncNotification(app)

    if (hasError) {
      broadcaster.sendBroadcast(buildStatusIntent(STATUS_ERROR).putExtra(EXTRA_ERROR, "Sync for ${shipments.size} item(s) failed"))

    } else {
      updateLastSyncTimestamp()
      broadcaster.sendBroadcast(buildStatusIntent(STATUS_FINISHED))
    }

    val elapsedTime = System.nanoTime() - startTime
    Log.i(TAG, "Sync finished in ${TimeUnit.NANOSECONDS.toMillis(elapsedTime)} ms")

    return !hasError
  }

  private fun updateLastSyncTimestamp() {
    PreferenceManager.getDefaultSharedPreferences(app)
      .edit()
      .putLong(app.getString(R.string.pref_key_last_sync), System.currentTimeMillis())
      .apply()
  }

  private fun buildStatusIntent(status: Int): Intent {
    val statusIntent = Intent(ACTION_SYNC).putExtra(EXTRA_STATUS, status)
    intent?.let { statusIntent.putExtras(intent) }

    return statusIntent
  }

  private fun isConnected(): Boolean {
    val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
  }

  private fun updateShipments(shipments: List<Shipment>) {
    val databaseHelper = (app as CarteiroApplication).databaseHelper

    shipments.forEach {
      databaseHelper.beginTransaction()

      databaseHelper.deletePostalRecords(it.number)
      databaseHelper.insertPostalRecords(it)
      databaseHelper.unreadPostalItem(it.number)

      databaseHelper.setTransactionSuccessful()
      databaseHelper.endTransaction()

      it.isUnread = true
    }
  }
}
