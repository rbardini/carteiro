package com.rbardini.carteiro.svc;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.alfredlibrary.AlfredException;
import org.alfredlibrary.utilitarios.correios.Rastreamento;
import org.alfredlibrary.utilitarios.correios.RegistroRastreamento;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Html;
import android.util.Log;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.ui.MainActivity;
import com.rbardini.carteiro.ui.PreferencesActivity.Preferences;
import com.rbardini.carteiro.ui.RecordActivity;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;

public class SyncService extends IntentService {
  private static final String TAG = "SyncService";

  public static final int STATUS_RUNNING = 1;
  public static final int STATUS_ERROR = 2;
  public static final int STATUS_FINISHED = 3;

  public static final int NOTIFICATION_ONGOING_SYNC = 1;
  public static final int NOTIFICATION_NEW_UPDATE = 2;

  private static boolean SYNC_CANCELED = false;

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
  };

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i(TAG, "Sync started");

    if (!CarteiroApplication.state.syncing) {
      CarteiroApplication.state.syncing = true;

      if (CarteiroApplication.state.receiver != null) {
        CarteiroApplication.state.receiver.send(STATUS_RUNNING, Bundle.EMPTY);
      }

      NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
      boolean shouldNotifySync = prefs.getBoolean(Preferences.NOTIFY_SYNC, false);
      if (shouldNotifySync) {
        notificationBuilder
          .setSmallIcon(R.drawable.ic_stat_sync)
          .setContentTitle(getString(R.string.notf_title_syncing))
          .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
          .addAction(R.drawable.ic_action_cancel, getString(R.string.negative_btn), PendingIntent.getBroadcast(this, 0, new Intent(this, CancelSyncReceiver.class), PendingIntent.FLAG_CANCEL_CURRENT))
          .setOngoing(true);
      }

      boolean update = false;

      String[] cods = null;
      Bundle extras = intent.getExtras();
      int flags;

      if (extras != null && extras.containsKey("cods")) {
        cods = extras.getStringArray("cods");
      } else {
        flags = 0;
        if (prefs.getBoolean(Preferences.SYNC_FAVORITES_ONLY, false)) { flags |= Category.FAVORITES; }
        if (prefs.getBoolean(Preferences.DONT_SYNC_DELIVERED_ITEMS, false)) { flags |= Category.UNDELIVERED; }
        cods = dh.getPostalItemCodes(flags);
      }

      SYNC_CANCELED = false;
      for (int i = 0; i < cods.length; i++) {
        if (SYNC_CANCELED) {
          SYNC_CANCELED = false;
          break;
        }

        String cod = cods[i];

        Log.i(TAG, "Syncing " + cod + "...");
        PostalItemRecord pir = new PostalItemRecord(cod).loadFrom(dh);

        if (shouldNotifySync) {
          notificationBuilder
            .setTicker(getString(R.string.notf_title_syncing))
            .setContentText(pir.getFullDesc())
            .setContentInfo(String.format(getString(R.string.notf_info_syncing), i + 1, cods.length))
            .setProgress(cods.length, i + 1, false);
          nm.notify(NOTIFICATION_ONGOING_SYNC, notificationBuilder.build());
        }

        try {
          List<RegistroRastreamento> list = Rastreamento.rastrear(cod);
          RegistroRastreamento lastReg = pir.getLatestPostalRecord().getReg();

          boolean hasUpdate = !lastReg.equals(list.get(0));
          boolean listSizeChanged = list.size() != pir.size();

          if (hasUpdate || listSizeChanged) {
            dh.beginTransaction();
            dh.deletePostalRecords(cod);
            for (int j = 0, length = list.size(); j < length; j++) {
              dh.insertPostalRecord(new PostalRecord(cod, length - j - 1, list.get(j)));
            }
            dh.setTransactionSuccessful();
            dh.endTransaction();

            if (hasUpdate) {
              app.addUpdatedCod(cod);
              update = true;
            }
          }
        } catch (AlfredException e) {
          Log.w(TAG, String.valueOf(e.getMessage()));
        } catch (Exception e) {
          Log.e(TAG, String.valueOf(e.getMessage()));
        }
      }

      nm.cancel(NOTIFICATION_ONGOING_SYNC);

      if (update && prefs.getBoolean(Preferences.NOTIFY, true)) {
        flags = 0;
        if (prefs.getBoolean(Preferences.NOTIFY_ALL, false)) {
          flags |= Category.ALL;
        } else {
          if (prefs.getBoolean(Preferences.NOTIFY_FAVORITES, true)) { flags |= Category.FAVORITES; }
          if (prefs.getBoolean(Preferences.NOTIFY_AVAILABLE, true)) { flags |= Category.AVAILABLE; }
          if (prefs.getBoolean(Preferences.NOTIFY_DELIVERED, true)) { flags |= Category.DELIVERED; }
          if (prefs.getBoolean(Preferences.NOTIFY_IRREGULAR, true)) { flags |= Category.IRREGULAR; }
          if (prefs.getBoolean(Preferences.NOTIFY_UNKNOWN, true)) { flags |= Category.UNKNOWN; }
          if (prefs.getBoolean(Preferences.NOTIFY_RETURNED, true)) { flags |= Category.RETURNED; }
        }
        if (flags != 0) { showNotification(flags); }
      }

      CarteiroApplication.state.syncing = false;
      if (CarteiroApplication.state.receiver != null) {
        CarteiroApplication.state.receiver.send(STATUS_FINISHED, Bundle.EMPTY);
      }
    }
    Log.i(TAG, "Sync finished");
  }

  private void showNotification(int flags) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    HashSet<PostalItem> postalItems = new HashSet<PostalItem>();
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
        .setSubText(pi.getLoc())
        .addAction(R.drawable.ic_action_place, getString(R.string.place_opt), PendingIntent.getActivity(this, 0, locateIntent, PendingIntent.FLAG_CANCEL_CURRENT))
        .addAction(R.drawable.ic_action_share, getString(R.string.share_opt), PendingIntent.getActivity(this, 0, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT));
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
      .setSound(Uri.parse(prefs.getString(Preferences.RINGTONE, "DEFAULT_SOUND")));

    if (prefs.getBoolean(Preferences.LIGHTS, true)) notificationBuilder.setLights(Color.YELLOW, 1000, 1200);
    if (prefs.getBoolean(Preferences.VIBRATE, true)) notificationBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

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
    long interval = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString(Preferences.REFRESH_INTERVAL, "3600000"));
    am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+interval, interval, getSender(context));
    Log.i(TAG, "Syncing scheduled");
  }

  public static void unscheduleSync(Context context) {
    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    am.cancel(getSender(context));
    Log.i(TAG, "Syncing unscheduled");
  }

  public static void cancelSync() {
    SyncService.SYNC_CANCELED = true;
  }
}
