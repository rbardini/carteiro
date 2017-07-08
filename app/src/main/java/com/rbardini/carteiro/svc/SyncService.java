package com.rbardini.carteiro.svc;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.ui.MainActivity;
import com.rbardini.carteiro.ui.RecordActivity;
import com.rbardini.carteiro.util.MobileTracker;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SyncService extends IntentService {
  private static final String TAG = "SyncService";

  public static final String ACTION_SYNC = TAG + ".ACTION_SYNC";
  public static final String EXTRA_STATUS = TAG + ".EXTRA_STATUS";
  public static final String EXTRA_RUNNING = TAG + ".EXTRA_RUNNING";
  public static final String EXTRA_ERROR = TAG + ".EXTRA_ERROR";

  public static final int STATUS_RUNNING = 1;
  public static final int STATUS_ERROR = 2;
  public static final int STATUS_FINISHED = 3;

  public static final int NOTIFICATION_ONGOING_SYNC = 1;
  public static final int NOTIFICATION_NEW_UPDATE = 2;

  private DatabaseHelper dh;
  private NotificationManager nm;
  private SharedPreferences prefs;
  private LocalBroadcastManager broadcaster;

  public SyncService() {
    super(TAG);
    setIntentRedelivery(true);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    dh = ((CarteiroApplication) getApplication()).getDatabaseHelper();
    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
    broadcaster = LocalBroadcastManager.getInstance(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (CarteiroApplication.syncing) {
      Log.i(TAG, "Sync already running");
      return;
    }

    if (!shouldSync(intent)) {
      Log.i(TAG, "Sync skipped");
      broadcaster.sendBroadcast(new Intent(ACTION_SYNC).putExtra(EXTRA_STATUS, STATUS_FINISHED));
      return;
    }

    long startTime = System.nanoTime();
    Log.i(TAG, "Sync started");

    broadcaster.sendBroadcast(new Intent(ACTION_SYNC)
      .putExtra(EXTRA_STATUS, STATUS_RUNNING)
      .putExtra(EXTRA_RUNNING, true));

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    boolean shouldNotifySync = prefs.getBoolean(getString(R.string.pref_key_notify_sync), false);
    if (shouldNotifySync) {
      notificationBuilder
        .setSmallIcon(R.drawable.ic_stat_sync)
        .setContentTitle(getString(R.string.notf_title_syncing))
        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
        .setOngoing(true);
    }

    boolean hasError = false;
    List<Shipment> updatedShipments = new ArrayList<>();

    List<Shipment> shipments;
    Bundle extras = intent.getExtras();

    if (extras != null && extras.containsKey("shipments")) {
      shipments = (List<Shipment>) extras.getSerializable("shipments");

    } else {
      shipments = dh.getShallowShipmentsForSync(getSyncFlags());
    }

    Map<String, ShipmentRecord> lastRecordMap = new HashMap<>();
    for (Shipment shipment : shipments) {
      lastRecordMap.put(shipment.getNumber(), shipment.getLastRecord());
    }

    try {
      Log.i(TAG, "Shallow-syncing " + shipments.size() + " item(s)...");

      if (shouldNotifySync) {
        notificationBuilder
          .setTicker(getString(R.string.notf_title_syncing))
          .setProgress(0, 0, true);
        nm.notify(NOTIFICATION_ONGOING_SYNC, notificationBuilder.build());
      }

      MobileTracker.shallowTrack(shipments, this);

      for (Shipment shipment : shipments) {
        if (shipment.isEmpty()) continue;

        ShipmentRecord newLastRecord = shipment.getLastRecord();
        ShipmentRecord lastRecord = lastRecordMap.get(shipment.getNumber());

        if (newLastRecord != null && !newLastRecord.equals(lastRecord)) {
          updatedShipments.add(shipment);
        }
      }

      if (!updatedShipments.isEmpty()) {
        Log.i(TAG, updatedShipments.size() + " update(s) found, deep-syncing item(s)...");
        MobileTracker.deepTrack(updatedShipments, this);

        Iterator<Shipment> it = updatedShipments.iterator();
        while (it.hasNext()) {
          Shipment updatedShipment = it.next();
          if (updatedShipment.isEmpty()) {
            it.remove();
            Log.w(TAG, "Dropped empty deep-synced item " + updatedShipment.getNumber());
          }
        }

        updateShipments(updatedShipments);
      }

    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      hasError = true;
    }

    nm.cancel(NOTIFICATION_ONGOING_SYNC);

    int notificationFlags = getNotificationFlags();
    if (!updatedShipments.isEmpty() && notificationFlags != 0) {
      showNotification(updatedShipments, notificationFlags);
    }

    if (hasError) {
      broadcaster.sendBroadcast(new Intent(ACTION_SYNC)
        .putExtra(EXTRA_STATUS, STATUS_ERROR)
        .putExtra(EXTRA_ERROR, "Request for " + shipments.size() + " items has failed"));

    } else {
      broadcaster.sendBroadcast(new Intent(ACTION_SYNC).putExtra(EXTRA_STATUS, STATUS_FINISHED));
    }

    long elapsedTime = System.nanoTime() - startTime;
    Log.i(TAG, "Sync finished in " + TimeUnit.NANOSECONDS.toMillis(elapsedTime) + " ms");
  }

  private boolean shouldSync(Intent intent) {
    Bundle extras = intent.getExtras();
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean syncWifiOnly = prefs.getBoolean(getString(R.string.pref_key_sync_wifi_only), false);
    boolean hasActiveNetwork = activeNetwork != null;

    boolean isManualSync = extras != null && extras.containsKey("shipments");
    boolean isConnected = hasActiveNetwork && activeNetwork.isConnected();
    boolean isWifi = hasActiveNetwork && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

    return isConnected && (isManualSync || isWifi || !syncWifiOnly);
  }

  private void updateShipments(List<Shipment> shipments) {
    for (Shipment shipment : shipments) {
      String cod = shipment.getNumber();

      dh.beginTransaction();

      dh.deletePostalRecords(cod);
      dh.insertPostalRecords(shipment);
      dh.unreadPostalItem(cod);

      dh.setTransactionSuccessful();
      dh.endTransaction();
    }
  }

  private int getSyncFlags() {
    int flags = 0;

    if (prefs.getBoolean(getString(R.string.pref_key_sync_favorites_only), false)) { flags |= Category.FAVORITES; }
    if (prefs.getBoolean(getString(R.string.pref_key_dont_sync_delivered_items), false)) { flags |= Category.UNDELIVERED; }

    return flags;
  }

  private int getNotificationFlags() {
    int flags = 0;

    if (prefs.getBoolean(getString(R.string.pref_key_notify), true)) {
      if (prefs.getBoolean(getString(R.string.pref_key_notify_all), false)) {
        flags |= Category.ALL;

      } else {
        if (prefs.getBoolean(getString(R.string.pref_key_notify_favorites), true)) { flags |= Category.FAVORITES; }
        if (prefs.getBoolean(getString(R.string.pref_key_notify_available), true)) { flags |= Category.AVAILABLE; }
        if (prefs.getBoolean(getString(R.string.pref_key_notify_delivered), true)) { flags |= Category.DELIVERED; }
        if (prefs.getBoolean(getString(R.string.pref_key_notify_irregular), true)) { flags |= Category.IRREGULAR; }
        if (prefs.getBoolean(getString(R.string.pref_key_notify_unknown), true)) { flags |= Category.UNKNOWN; }
        if (prefs.getBoolean(getString(R.string.pref_key_notify_returned), true)) { flags |= Category.RETURNED; }
      }
    }

    return flags;
  }

  private void showNotification(List<Shipment> updatedShipments, int flags) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    HashSet<Shipment> shipments = new HashSet<>();
    String ticker, title, desc;
    long date;
    Intent intent;

    for (Shipment shipment : updatedShipments) {
      if (notifiable(shipment, flags)) {
        shipments.add(shipment);
      }
    }

    int count = shipments.size();
    if (count == 0) { return; }
    if (count == 1) {
      Shipment shipment = (Shipment) shipments.toArray()[0];
      ShipmentRecord lastRecord = shipment.getLastRecord();

      ticker = String.format(getString(R.string.notf_tckr_single_obj), shipment.getDescription(), lastRecord.getStatus().toLowerCase(Locale.getDefault()));
      title = shipment.getDescription();
      desc = lastRecord.getStatus();
      date = lastRecord.getDate().getTime();
      intent = new Intent(this, RecordActivity.class).putExtra("postalItem", shipment);

      NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle(notificationBuilder);

      Intent locateIntent = new Intent(this, RecordActivity.class).putExtra("postalItem", shipment).setAction("locate");
      Intent shareIntent = new Intent(this, RecordActivity.class).putExtra("postalItem", shipment).setAction("share");

      notificationBuilder
        .setColor(ContextCompat.getColor(this, UIUtils.getPostalStatusColor(lastRecord.getStatus())))
        .setSubText(lastRecord.getLocal())
        .addAction(R.drawable.ic_place_white_24dp, getString(R.string.opt_view_place), PendingIntent.getActivity(this, 0, locateIntent, PendingIntent.FLAG_CANCEL_CURRENT))
        .addAction(R.drawable.ic_share_white_24dp, getString(R.string.opt_share), PendingIntent.getActivity(this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT));
      notificationStyle.bigText(lastRecord.getDescription());
    } else {
      Iterator<Shipment> iterator = shipments.iterator();
      ticker = title = String.format(getString(R.string.notf_tckr_multi_obj), count);
      desc = "";
      date = System.currentTimeMillis();
      intent = new Intent(this, MainActivity.class);

      NotificationCompat.InboxStyle notificationStyle = new NotificationCompat.InboxStyle(notificationBuilder);
      notificationBuilder.setNumber(count);
      int deliveredCount = 0;
      while (iterator.hasNext()) {
        Shipment shipment = iterator.next();
        ShipmentRecord lastRecord = shipment.getLastRecord();

        notificationStyle.addLine(Html.fromHtml(String.format(getString(R.string.notf_line_multi_obj), shipment.getDescription(), lastRecord.getStatus())));
        desc += shipment.getDescription() + (iterator.hasNext() ? ", " : "");
        if (PostalUtils.Status.getCategory(lastRecord.getStatus()) == PostalUtils.Category.DELIVERED) deliveredCount++;
      }
      notificationStyle.setSummaryText(getResources().getQuantityString(R.plurals.notf_summ_multi_obj, deliveredCount, deliveredCount));
    }

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    stackBuilder.addNextIntentWithParentStack(intent);
    PendingIntent pending = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);

    notificationBuilder
      .setSmallIcon(R.drawable.ic_stat_notify)
      .setTicker(ticker)
      .setContentTitle(title)
      .setContentText(desc)
      .setContentIntent(pending)
      .setWhen(date)
      .setAutoCancel(true)
      .setSound(Uri.parse(prefs.getString(getString(R.string.pref_key_ringtone), "DEFAULT_SOUND")));

    if (prefs.getBoolean(getString(R.string.pref_key_lights), true)) notificationBuilder.setLights(Color.YELLOW, 1000, 1200);
    if (prefs.getBoolean(getString(R.string.pref_key_vibrate), true)) notificationBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

    nm.notify(NOTIFICATION_NEW_UPDATE, notificationBuilder.build());
  }

  private boolean notifiable(Shipment shipment, int flags) {
    return ((Category.ALL & flags) != 0)
      || (((Category.FAVORITES & flags) != 0) && shipment.isFavorite())
      || ((Status.getCategory(shipment.getLastRecord().getStatus()) & flags) != 0);
  }

  private static PendingIntent getSender(Context context) {
    Intent sync = new Intent(Intent.ACTION_SYNC, null, context, SyncService.class);
    return PendingIntent.getService(context, 0, sync, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  public static void scheduleSync(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    long interval = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_refresh_interval), "3600000"));
    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+interval, interval, getSender(context));
    Log.i(TAG, "Syncing scheduled: " + interval);
  }

  public static void unscheduleSync(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.cancel(getSender(context));
    Log.i(TAG, "Syncing unscheduled");
  }
}
