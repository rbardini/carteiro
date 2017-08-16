package com.rbardini.carteiro.util

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.preference.PreferenceManager
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.TextUtils
import com.rbardini.carteiro.R
import com.rbardini.carteiro.model.Shipment
import com.rbardini.carteiro.ui.MainActivity
import com.rbardini.carteiro.ui.RecordActivity
import com.rbardini.carteiro.util.PostalUtils.Category
import com.rbardini.carteiro.util.PostalUtils.Status
import java.util.*

object NotificationUtils {
  private const val NOTIFICATION_ONGOING_SYNC_ID = 1
  private const val NOTIFICATION_UPDATE_SUMMARY_ID = 2

  private const val NOTIFICATION_NEW_UPDATE_TAG = "NOTIFICATION_NEW_UPDATE"
  private const val NOTIFICATION_UPDATE_GROUP_KEY = "NOTIFICATION_UPDATE_GROUP"

  fun notifyOngoingSyncIfAllowed(context: Context) {
    if (shouldNotifyOnGoingSync(context)) notifyOngoingSync(context)
  }

  fun notifyOngoingSync(context: Context) {
    val notification = Notification.Builder(context)
      .setSmallIcon(R.drawable.ic_stat_sync)
      .setContentTitle(context.getString(R.string.notf_title_syncing))
      .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))
      .setTicker(context.getString(R.string.notf_title_syncing))
      .setProgress(0, 0, true)
      .setOngoing(true)

    getNotificationManager(context).notify(NOTIFICATION_ONGOING_SYNC_ID, notification.build())
  }

  fun cancelOngoingSync(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_ONGOING_SYNC_ID)

  fun notifyShipmentUpdatesIfAllowed(context: Context, shipments: List<Shipment>) {
    if (shipments.isEmpty()) return

    val flags = getNotificationFlags(context)
    if (flags == 0) return

    val notifiableShipments = shipments.filter { shouldNotifyShipmentUpdate(it, flags) }
    if (notifiableShipments.isEmpty()) return

    val shouldBundle = notifiableShipments.size > 1
    notifiableShipments.forEach { notifyShipmentUpdate(context, it, shouldBundle) }
    if (shouldBundle) notifyShipmentUpdates(context, notifiableShipments)
  }

  private fun notifyShipmentUpdate(context: Context, shipment: Shipment, bundled: Boolean) {
    val lastRecord = shipment.getLastRecord()
    val ticker = String.format(context.getString(R.string.notf_tckr_single_obj), shipment.getDescription(), lastRecord?.status?.toLowerCase(Locale.getDefault()))
    val intent = Intent(context, RecordActivity::class.java).putExtra(RecordActivity.EXTRA_SHIPMENT, shipment)

    val notification = getBaseShipmentUpdateNotificationBuilder(context, intent, !bundled)
      .setStyle(Notification.BigTextStyle().bigText(lastRecord?.getDescription()))
      .setTicker(ticker)
      .setContentTitle(shipment.getDescription())
      .setContentText(lastRecord?.status)
      .setWhen(lastRecord?.date?.time!!)
      .setColor(ContextCompat.getColor(context, UIUtils.getPostalStatusColor(lastRecord.status)))
      .setSubText(lastRecord.local)
      .addAction(Notification.Action.Builder(R.drawable.ic_place_white_24dp, context.getString(R.string.opt_view_place),
          getNotificationActionIntent(context, intent, shipment, RecordActivity.ACTION_LOCATE)).build())
      .addAction(Notification.Action.Builder(R.drawable.ic_share_white_24dp, context.getString(R.string.opt_share),
          getNotificationActionIntent(context, intent, shipment, RecordActivity.ACTION_SHARE)).build())

    getNotificationManager(context).notify(NOTIFICATION_NEW_UPDATE_TAG, shipment.number.hashCode(), notification.build())
  }

  private fun notifyShipmentUpdates(context: Context, shipments: List<Shipment>) {
    val deliveredCount = shipments.count { Status.getCategory(it.getLastRecord()?.status) == Category.DELIVERED }
    val ticker = String.format(context.getString(R.string.notf_tckr_multi_obj), shipments.size)
    val intent = Intent(context, MainActivity::class.java)

    val style = Notification.InboxStyle().setSummaryText(context.resources.getQuantityString(R.plurals.notf_summ_multi_obj, deliveredCount, deliveredCount))
    val lineFormat = context.getString(R.string.notf_line_multi_obj)
    val contentText = TextUtils.join(", ", shipments.map {
      style.addLine(Html.fromHtml(String.format(lineFormat, it.getDescription(), it.getLastRecord()?.status)))
      it.getDescription()
    })

    val notification = getBaseShipmentUpdateNotificationBuilder(context, intent, true)
      .setStyle(style)
      .setTicker(ticker)
      .setContentTitle(ticker)
      .setContentText(contentText)
      .setWhen(System.currentTimeMillis())
      .setNumber(shipments.size)
      .setGroupSummary(true)

    getNotificationManager(context).notify(NOTIFICATION_UPDATE_SUMMARY_ID, notification.build())
  }

  private fun getBaseShipmentUpdateNotificationBuilder(context: Context, intent: Intent, unique: Boolean): Notification.Builder {
    val prefs = getSharedPreferences(context)
    val stackBuilder = TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)

    val notification = Notification.Builder(context)
      .setSmallIcon(R.drawable.ic_stat_notify)
      .setContentIntent(stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT))
      .setAutoCancel(true)
      .setGroup(NOTIFICATION_UPDATE_GROUP_KEY)

    if (unique) {
      notification.setSound(Uri.parse(prefs.getString(context.getString(R.string.pref_key_ringtone), "DEFAULT_SOUND")))
      if (prefs.getBoolean(context.getString(R.string.pref_key_lights), true)) notification.setLights(Color.YELLOW, 1000, 1200)
      if (prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true)) notification.setDefaults(Notification.DEFAULT_VIBRATE)
    }

    return notification
  }

  @JvmStatic
  fun cancelShipmentUpdates(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_UPDATE_SUMMARY_ID)

  private fun getSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

  private fun getNotificationActionIntent(context: Context, intent: Intent, shipment: Shipment, action: String) =
    PendingIntent.getActivity(context, shipment.number.hashCode(), Intent(intent).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT)

  private fun shouldNotifyOnGoingSync(context: Context) =
    getSharedPreferences(context).getBoolean(context.getString(R.string.pref_key_notify_sync), false)

  private fun getNotificationManager(context: Context) =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  private fun getNotificationFlags(context: Context): Int {
    val prefs = getSharedPreferences(context)
    var flags = 0

    if (!prefs.getBoolean(context.getString(R.string.pref_key_notify), true)) {
      return flags
    }

    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_all), false)) {
      return flags or Category.ALL
    }

    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_favorites), true)) flags = flags or Category.FAVORITES
    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_available), true)) flags = flags or Category.AVAILABLE
    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_delivered), true)) flags = flags or Category.DELIVERED
    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_irregular), true)) flags = flags or Category.IRREGULAR
    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_unknown), true)) flags = flags or Category.UNKNOWN
    if (prefs.getBoolean(context.getString(R.string.pref_key_notify_returned), true)) flags = flags or Category.RETURNED

    return flags
  }

  private fun shouldNotifyShipmentUpdate(shipment: Shipment, flags: Int): Boolean =
    Category.ALL and flags != 0 ||
    Category.FAVORITES and flags != 0 && shipment.isFavorite ||
    Status.getCategory(shipment.getLastRecord()!!.status) and flags != 0
}
