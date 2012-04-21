package com.rbardini.carteiro.svc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import android.util.Log;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.PostalRecord;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.ui.MainActivity;
import com.rbardini.carteiro.ui.PreferencesActivity.Preferences;
import com.rbardini.carteiro.ui.RecordActivity;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;

public class SyncService extends IntentService {
  private static final String TAG = "SyncService";

  public static final int STATUS_RUNNING = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_FINISHED = 3;

    private CarteiroApplication app;
  private DatabaseHelper dh;

  public SyncService() {
    super(TAG);
    setIntentRedelivery(true);
  }

  @Override
  public void onCreate() {
    super.onCreate();

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();
  };

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i(TAG, "Syncing...");
    if (!CarteiroApplication.state.syncing) {
      CarteiroApplication.state.syncing = true;

          if (CarteiroApplication.state.receiver != null) {
            CarteiroApplication.state.receiver.send(STATUS_RUNNING, Bundle.EMPTY);
          }

          boolean update = false;

          String[] cods = null;
          Bundle extras = intent.getExtras();
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
          int flags;

          if (extras != null && extras.containsKey("cods")) {
            cods = extras.getStringArray("cods");
          } else {
            flags = 0;
            if (prefs.getBoolean(Preferences.SYNC_FAVORITES_ONLY, false)) { flags |= Category.FAVORITES; }
            if (prefs.getBoolean(Preferences.DONT_SYNC_DELIVERED_ITEMS, false)) { flags |= Category.UNDELIVERED; }
            cods = dh.getPostalItemCodes(flags);
      }
      for (String cod : cods) {
        //Log.i(TAG, "Syncing "+cod+"...");
        try {
          List<RegistroRastreamento> list = Rastreamento.rastrear(cod);
          RegistroRastreamento lastReg = dh.getLastPostalRecord(cod).getReg();
          if (!lastReg.equals(list.get(0))) {
            dh.beginTransaction();
            for (int i=0, length=list.size(); i<length; i++) {
              dh.insertPostalRecord(new PostalRecord(cod, length-i-1, list.get(i)));
            }
            dh.setTransactionSuccessful();
            dh.endTransaction();
            app.addUpdatedCod(cod);
            update = true;
          }
        } catch (AlfredException e) {
          // "O sistema dos Correios n�o possui dados sobre o objeto informado"
        } catch (Exception e) {
          Log.e(TAG, e.getMessage());
        }
      }
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
        if (flags != 0) { showNotification(prefs, flags); }
      }

      CarteiroApplication.state.syncing = false;
      if (CarteiroApplication.state.receiver != null) {
        CarteiroApplication.state.receiver.send(STATUS_FINISHED, Bundle.EMPTY);
      }
    }
    Log.i(TAG, "Synced");
  }

  private void showNotification(SharedPreferences prefs, int flags) {
    Set<String> updatedCods = app.getUpdatedCods();
    HashSet<PostalItem> postalItems = new HashSet<PostalItem>();
    String ticker, title, desc;
    Intent intent;

    for (String cod : updatedCods) {
      PostalItem pi = dh.getPostalItem(cod);
      if (notifiable(pi, flags)) {
        postalItems.add(pi);
      }
    }

    int count = postalItems.size();
    if (count == 0) { return; }
    if (count == 1) {
      PostalItem pi = (PostalItem) postalItems.toArray()[0];
      ticker = String.format(getString(R.string.notf_tckr_single_obj), pi.getSafeDesc(), pi.getStatus().toLowerCase());
      title = pi.getSafeDesc();
      desc = pi.getStatus();
      intent = new Intent(this, RecordActivity.class);
      intent.putExtra("postalItem", pi);
    } else {
      ticker = String.format(getString(R.string.notf_tckr_multi_obj), count);
      title = getString(R.string.notf_title_multi_obj);
      desc = String.format(getString(R.string.notf_desc_multi_obj), count);
      intent = new Intent(this, MainActivity.class);
    }

    PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
      .setSmallIcon(R.drawable.ic_stat_notify)
      .setTicker(ticker)
      .setNumber(count)
      .setContentTitle(title)
      .setContentText(desc)
      .setContentIntent(pending)
      .setWhen(System.currentTimeMillis())
      .setAutoCancel(true)
      .setSound(Uri.parse(prefs.getString(Preferences.RINGTONE, "DEFAULT_SOUND")));
    if (prefs.getBoolean(Preferences.LIGHTS, true)) { builder.setLights(Color.YELLOW, 300, 1000); }
    if (prefs.getBoolean(Preferences.VIBRATE, true)) { builder.setDefaults(Notification.DEFAULT_VIBRATE); }

    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    nm.notify(R.string.app_name, builder.getNotification());
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
}