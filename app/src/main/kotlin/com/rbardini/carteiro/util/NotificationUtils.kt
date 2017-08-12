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
  const val NOTIFICATION_ONGOING_SYNC = 1
  const val NOTIFICATION_NEW_UPDATE = 2

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

    getNotificationManager(context).notify(NOTIFICATION_ONGOING_SYNC, notification.build())
  }

  fun cancelOngoingSync(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_ONGOING_SYNC)

  fun notifyShipmentUpdatesIfAllowed(context: Context, shipments: List<Shipment>) {
    if (shipments.isEmpty()) return

    val flags = getNotificationFlags(context)
    if (flags == 0) return

    val notifiableShipments = shipments.filter { shouldNotifyShipmentUpdate(it, flags) }
    if (notifiableShipments.isEmpty()) return

    val prefs = getSharedPreferences(context)
    val notification = Notification.Builder(context)

    val ticker: String
    val title: String?
    val desc: String?
    val date: Long?
    val intent: Intent
    val style: Notification.Style

    if (notifiableShipments.size == 1) {
      val shipment = notifiableShipments[0]
      val lastRecord = shipment.getLastRecord()
      val actionIntent = Intent(context, RecordActivity::class.java).putExtra("shipment", shipment)

      ticker = String.format(context.getString(R.string.notf_tckr_single_obj), shipment.getDescription(), lastRecord?.status?.toLowerCase(Locale.getDefault()))
      title = shipment.getDescription()
      desc = lastRecord?.status
      date = lastRecord?.date?.time
      intent = Intent(context, RecordActivity::class.java).putExtra("shipment", shipment)
      style = Notification.BigTextStyle().bigText(lastRecord?.getDescription())

      notification
        .setColor(ContextCompat.getColor(context, UIUtils.getPostalStatusColor(lastRecord?.status)))
        .setSubText(lastRecord?.local)
        .addAction(Notification.Action.Builder(R.drawable.ic_place_white_24dp, context.getString(R.string.opt_view_place),
            PendingIntent.getActivity(context, 0, Intent(actionIntent).setAction("locate"), PendingIntent.FLAG_CANCEL_CURRENT)).build())
        .addAction(Notification.Action.Builder(R.drawable.ic_share_white_24dp, context.getString(R.string.opt_share),
            PendingIntent.getActivity(context, 0, Intent(actionIntent).setAction("share"), PendingIntent.FLAG_CANCEL_CURRENT)).build())

    } else {
      val deliveredCount = notifiableShipments.count { Status.getCategory(it.getLastRecord()?.status) == Category.DELIVERED }
      val lineFormat = context.getString(R.string.notf_line_multi_obj)

      ticker = String.format(context.getString(R.string.notf_tckr_multi_obj), notifiableShipments.size)
      title = ticker
      date = System.currentTimeMillis()
      intent = Intent(context, MainActivity::class.java)
      style = Notification.InboxStyle().setSummaryText(context.resources.getQuantityString(R.plurals.notf_summ_multi_obj, deliveredCount, deliveredCount))

      desc = TextUtils.join(", ", notifiableShipments.map {
        style.addLine(Html.fromHtml(String.format(lineFormat, it.getDescription(), it.getLastRecord()?.status)))
        it.getDescription()
      })
    }

    val stackBuilder = TaskStackBuilder.create(context)
    stackBuilder.addNextIntentWithParentStack(intent)
    val pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT)

    notification
      .setStyle(style)
      .setSmallIcon(R.drawable.ic_stat_notify)
      .setTicker(ticker)
      .setContentTitle(title)
      .setContentText(desc)
      .setContentIntent(pending)
      .setWhen(date!!)
      .setAutoCancel(true)
      .setSound(Uri.parse(prefs.getString(context.getString(R.string.pref_key_ringtone), "DEFAULT_SOUND")))

    if (prefs.getBoolean(context.getString(R.string.pref_key_lights), true)) notification.setLights(Color.YELLOW, 1000, 1200)
    if (prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true)) notification.setDefaults(Notification.DEFAULT_VIBRATE)

    getNotificationManager(context).notify(NOTIFICATION_NEW_UPDATE, notification.build())
  }

  @JvmStatic
  fun cancelShipmentUpdates(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_NEW_UPDATE)

  private fun getSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

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
