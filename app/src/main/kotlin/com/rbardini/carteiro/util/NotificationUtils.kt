package com.rbardini.carteiro.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.text.Html
import android.text.TextUtils
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.rbardini.carteiro.R
import com.rbardini.carteiro.model.Shipment
import com.rbardini.carteiro.ui.MainActivity
import com.rbardini.carteiro.ui.RecordActivity
import com.rbardini.carteiro.util.PostalUtils.Category
import com.rbardini.carteiro.util.PostalUtils.Status
import java.util.Locale

object NotificationUtils {
  private const val NOTIFICATION_ID_ONGOING_SYNC = 1
  private const val NOTIFICATION_ID_UPDATE_SUMMARY = 2

  private const val NOTIFICATION_CHANNEL_ID_ONGOING_SYNC = "NOTIFICATION_CHANNEL_ID_ONGOING_SYNC";
  private const val NOTIFICATION_CHANNEL_ID_ITEM_UPDATE = "NOTIFICATION_CHANNEL_ID_ITEM_UPDATE";

  private const val NOTIFICATION_TAG_SINGLE_ITEM_UPDATE = "NOTIFICATION_TAG_SINGLE_ITEM_UPDATE"
  private const val NOTIFICATION_GROUP_KEY_UPDATE = "NOTIFICATION_GROUP_KEY_UPDATE"

  @JvmStatic
  fun getNotificationRingtoneValue(context: Context): String? =
    getSharedPreferences(context).getString(
      context.getString(R.string.pref_key_ringtone),
      Settings.System.DEFAULT_NOTIFICATION_URI.toString()
    )

  @JvmStatic
  fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = getNotificationManager(context)
    if (notificationManager.notificationChannels.isNotEmpty()) return

    val ongoingSyncChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID_ONGOING_SYNC,
        context.getString(R.string.notf_channel_sync), NotificationManager.IMPORTANCE_LOW)

    val updateChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID_ITEM_UPDATE,
        context.getString(R.string.notf_channel_update), NotificationManager.IMPORTANCE_DEFAULT)
    updateChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
    updateChannel.lightColor = Color.YELLOW

    val prefs = getSharedPreferences(context)
    if (prefs.getBoolean(context.getString(R.string.pref_key_lights), true)) updateChannel.enableLights(true)
    if (prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true)) updateChannel.enableVibration(true)

    notificationManager.createNotificationChannels(listOf(ongoingSyncChannel, updateChannel))
  }

  fun notifyOngoingSyncIfAllowed(context: Context) {
    if (shouldNotifyOnGoingSync(context)) notifyOngoingSync(context)
  }

  fun notifyOngoingSync(context: Context) {
    val notification = getBaseNotificationBuilder(context, NOTIFICATION_CHANNEL_ID_ONGOING_SYNC)
      .setSmallIcon(R.drawable.ic_stat_sync)
      .setContentTitle(context.getString(R.string.notf_title_syncing))
      .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))
      .setTicker(context.getString(R.string.notf_title_syncing))
      .setProgress(0, 0, true)
      .setOngoing(true)

    getNotificationManager(context).notify(NOTIFICATION_ID_ONGOING_SYNC, notification.build())
  }

  fun cancelOngoingSyncNotification(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_ID_ONGOING_SYNC)

  fun notifyShipmentUpdatesIfAllowed(context: Context, shipments: List<Shipment>) {
    if (shipments.isEmpty()) return

    val flags = getNotificationFlags(context)
    if (flags == 0) return

    val notifiableShipments = shipments.filter { shouldNotifyShipmentUpdate(it, flags) }
    if (notifiableShipments.isEmpty()) return

    notifiableShipments.forEach { notifyShipmentUpdate(context, it) }
    if (notifiableShipments.size > 1) notifyShipmentUpdates(context, notifiableShipments)
  }

  private fun notifyShipmentUpdate(context: Context, shipment: Shipment) {
    val lastRecord = shipment.getLastRecord()
    val ticker = String.format(context.getString(R.string.notf_tckr_single_obj), shipment.getDescription(), lastRecord?.status?.toLowerCase(Locale.getDefault()))
    val intent = Intent(context, RecordActivity::class.java).putExtra(RecordActivity.EXTRA_SHIPMENT, shipment)
    val requestCode = shipment.number.hashCode()

    val notification = getBaseShipmentUpdateNotificationBuilder(context, intent, requestCode)
      .setStyle(Notification.BigTextStyle().bigText(lastRecord?.getDescription()))
      .setTicker(ticker)
      .setContentTitle(shipment.getDescription())
      .setContentText(lastRecord?.status)
      .setWhen(lastRecord?.date?.time!!)
      .setColor(ContextCompat.getColor(context, UIUtils.getPostalStatusColor(lastRecord.status)))
      .setSubText(lastRecord.local)
      .addAction(Notification.Action.Builder(R.drawable.ic_place_white_24dp, context.getString(R.string.opt_view_place),
          getNotificationActionIntent(context, intent, requestCode, RecordActivity.ACTION_LOCATE)).build())
      .addAction(Notification.Action.Builder(R.drawable.ic_share_white_24dp, context.getString(R.string.opt_share),
          getNotificationActionIntent(context, intent, requestCode, RecordActivity.ACTION_SHARE)).build())

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      val prefs = getSharedPreferences(context)
      val ringtone = getNotificationRingtoneValue(context)

      notification.setSound(Uri.parse(ringtone))
      if (prefs.getBoolean(context.getString(R.string.pref_key_lights), true)) notification.setLights(Color.YELLOW, 1000, 1200)
      if (prefs.getBoolean(context.getString(R.string.pref_key_vibrate), true)) notification.setDefaults(Notification.DEFAULT_VIBRATE)
    }

    getNotificationManager(context).notify(NOTIFICATION_TAG_SINGLE_ITEM_UPDATE, requestCode, notification.build())
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

    val notification = getBaseShipmentUpdateNotificationBuilder(context, intent, NOTIFICATION_ID_UPDATE_SUMMARY)
      .setStyle(style)
      .setTicker(ticker)
      .setContentTitle(ticker)
      .setContentText(contentText)
      .setWhen(System.currentTimeMillis())
      .setNumber(shipments.size)
      .setGroupSummary(true)

    getNotificationManager(context).notify(NOTIFICATION_ID_UPDATE_SUMMARY, notification.build())
  }

  private fun getBaseNotificationBuilder(context: Context, channelId: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(context, channelId) else Notification.Builder(context)

  private fun getBaseShipmentUpdateNotificationBuilder(context: Context, intent: Intent, requestCode: Int): Notification.Builder {
    val stackBuilder = TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)

    return getBaseNotificationBuilder(context, NOTIFICATION_CHANNEL_ID_ITEM_UPDATE)
      .setSmallIcon(R.drawable.ic_stat_notify)
      .setContentIntent(stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT))
      .setAutoCancel(true)
      .setGroup(NOTIFICATION_GROUP_KEY_UPDATE)
  }

  @JvmStatic
  fun cancelShipmentUpdateNotifications(context: Context) = getNotificationManager(context).cancel(NOTIFICATION_ID_UPDATE_SUMMARY)

  private fun getSharedPreferences(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

  private fun getNotificationActionIntent(context: Context, intent: Intent, requestCode: Int, action: String) =
    PendingIntent.getActivity(context, requestCode, Intent(intent).setAction(action), PendingIntent.FLAG_UPDATE_CURRENT)

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
