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
import android.text.Html;
import android.util.Log;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.ui.MainActivity;
import com.rbardini.carteiro.ui.RecordActivity;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.Tracker;
import com.rbardini.carteiro.util.UIUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SyncService extends IntentService {
  private static final String TAG = "SyncService";

  public static final int STATUS_RUNNING = 1;
  public static final int STATUS_ERROR = 2;
  public static final int STATUS_FINISHED = 3;

  public static final int NOTIFICATION_ONGOING_SYNC = 1;
  public static final int NOTIFICATION_NEW_UPDATE = 2;

  private CarteiroApplication app;
  private DatabaseHelper dh;
  private NotificationManager nm;
  private SharedPreferences prefs;

  public SyncService() {
    super(TAG);
    setIntentRedelivery(true);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();
    nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    prefs = PreferenceManager.getDefaultSharedPreferences(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (CarteiroApplication.state.syncing) {
      Log.i(TAG, "Sync already running");
      return;
    }

    if (!shouldSync(intent)) {
      Log.i(TAG, "Sync skipped");

      if (CarteiroApplication.state.receiver != null) {
        CarteiroApplication.state.receiver.send(STATUS_FINISHED, Bundle.EMPTY);
      }

      return;
    }

    Log.i(TAG, "Sync started");

    CarteiroApplication.state.syncing = true;

    if (CarteiroApplication.state.receiver != null) {
      CarteiroApplication.state.receiver.send(STATUS_RUNNING, Bundle.EMPTY);
    }

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
    boolean hasUpdate = false;

    String[] cods;
    Bundle extras = intent.getExtras();

    if (extras != null && extras.containsKey("cods")) {
      cods = extras.getStringArray("cods");

    } else {
      cods = dh.getPostalItemCodes(getSyncFlags());
    }

    try {
      Log.i(TAG, "Syncing " + cods.length + " items...");

      if (shouldNotifySync) {
        notificationBuilder
          .setTicker(getString(R.string.notf_title_syncing))
          .setProgress(0, 0, true);
        nm.notify(NOTIFICATION_ONGOING_SYNC, notificationBuilder.build());
      }

      List<List<PostalRecord>> prLists = Tracker.track(cods);

      for (List<PostalRecord> prList : prLists) {
        if (prList.isEmpty()) continue;

        int newListSize = prList.size();
        PostalRecord newLastRecord = prList.get(newListSize - 1);

        String cod = newLastRecord.getCod();

        PostalItemRecord pir = new PostalItemRecord(cod).loadFrom(dh);
        PostalRecord oldLastRecord = pir.getLastPostalRecord();
        int oldListSize = pir.size();

        boolean hasRecord = newListSize > 0;
        boolean itemUpdated = hasRecord && !oldLastRecord.equals(newLastRecord);
        boolean listSizeChanged = hasRecord && newListSize != oldListSize;

        if (itemUpdated || listSizeChanged) {
          pir.setPostalRecords(prList);
          updatePostalItem(cod, pir);

          if (itemUpdated) {
            app.addUpdatedCod(cod);
            hasUpdate = true;
          }
        }
      }
    } catch (Exception e) {
      Log.e(TAG, e.getMessage());
      hasError = true;
    }

    nm.cancel(NOTIFICATION_ONGOING_SYNC);

    int notificationFlags = getNotificationFlags();
    if (hasUpdate && notificationFlags != 0) {
      showNotification(notificationFlags);
    }

    CarteiroApplication.state.syncing = false;
    if (CarteiroApplication.state.receiver != null) {
      if (hasError) {
        Bundle resultData = new Bundle();
        resultData.putString(Intent.EXTRA_TEXT, "Request for " + cods.length + " items has failed");
        CarteiroApplication.state.receiver.send(STATUS_ERROR, resultData);

      } else {
        CarteiroApplication.state.receiver.send(STATUS_FINISHED, Bundle.EMPTY);
      }
    }

    Log.i(TAG, "Sync finished");
  }

  private boolean shouldSync(Intent intent) {
    Bundle extras = intent.getExtras();
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean syncWifiOnly = prefs.getBoolean(getString(R.string.pref_key_sync_wifi_only), false);
    boolean hasActiveNetwork = activeNetwork != null;

    boolean isManualSync = extras != null && extras.containsKey("cods");
    boolean isConnected = hasActiveNetwork && activeNetwork.isConnected();
    boolean isWifi = hasActiveNetwork && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;

    return isConnected && (isManualSync || isWifi || !syncWifiOnly);
  }

  private void updatePostalItem(String cod, PostalItemRecord pir) {
    dh.beginTransaction();

    dh.deletePostalRecords(cod);
    dh.insertPostalRecords(pir.getPostalRecords());

    dh.setTransactionSuccessful();
    dh.endTransaction();
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

  private void showNotification(int flags) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    HashSet<PostalItem> postalItems = new HashSet<>();
    String ticker, title, desc;
    long date;
    Intent intent;

    for (String cod : app.getUpdatedCods()) {
      PostalItem pi = dh.getPostalItem(cod);
      if (notifiable(pi, flags)) {
        postalItems.add(pi);
      }
    }

    int count = postalItems.size();
    if (count == 0) { return; }
    if (count == 1) {
      PostalItem pi = (PostalItem) postalItems.toArray()[0];
      ticker = String.format(getString(R.string.notf_tckr_single_obj), pi.getSafeDesc(), pi.getStatus().toLowerCase(Locale.getDefault()));
      title = pi.getSafeDesc();
      desc = pi.getStatus();
      date = pi.getDate().getTime();
      intent = new Intent(this, RecordActivity.class).putExtra("postalItem", pi);

      NotificationCompat.BigTextStyle notificationStyle = new NotificationCompat.BigTextStyle(notificationBuilder);

      Intent locateIntent = new Intent(this, RecordActivity.class).putExtra("postalItem", pi).setAction("locate");
      Intent shareIntent = new Intent(this, RecordActivity.class).putExtra("postalItem", pi).setAction("share");

      notificationBuilder
        .setColor(ContextCompat.getColor(this, UIUtils.getPostalStatusColor(pi.getStatus())))
        .setSubText(pi.getLoc())
        .addAction(R.drawable.ic_place_white_24dp, getString(R.string.opt_view_place), PendingIntent.getActivity(this, 0, locateIntent, PendingIntent.FLAG_CANCEL_CURRENT))
        .addAction(R.drawable.ic_share_white_24dp, getString(R.string.opt_share), PendingIntent.getActivity(this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT));
      notificationStyle.bigText(pi.getFullInfo());
    } else {
      Iterator<PostalItem> iterator = postalItems.iterator();
      ticker = title = String.format(getString(R.string.notf_tckr_multi_obj), count);
      desc = "";
      date = System.currentTimeMillis();
      intent = new Intent(this, MainActivity.class);

      NotificationCompat.InboxStyle notificationStyle = new NotificationCompat.InboxStyle(notificationBuilder);
      notificationBuilder.setNumber(count);
      int deliveredCount = 0;
      while (iterator.hasNext()) {
        PostalItem pi = iterator.next();
        notificationStyle.addLine(Html.fromHtml(String.format(getString(R.string.notf_line_multi_obj), pi.getSafeDesc(), pi.getStatus())));
        desc += pi.getSafeDesc() + (iterator.hasNext() ? ", " : "");
        if (PostalUtils.Status.getCategory(pi.getStatus()) == PostalUtils.Category.DELIVERED) deliveredCount++;
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

  private boolean notifiable(PostalItem pi, int flags) {
    return ((Category.ALL & flags) != 0)
      || (((Category.FAVORITES & flags) != 0) && pi.isFav())
      || ((Status.getCategory(pi.getStatus()) & flags) != 0);
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
