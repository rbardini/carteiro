package com.rbardini.carteiro.ui;

import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.svc.BackupManagerWrapper;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.UIUtils;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
  private static boolean backupManagerAvailable;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    try {
      BackupManagerWrapper.checkAvailable();
      backupManagerAvailable = true;
    } catch (Throwable t) {
      backupManagerAvailable = false;
    }

    setRefreshIntervalPreference();
    setNotificationSoundPreference();
    setAboutPreference();
    setVersionPreference();

    Preference ringPref = findPreference(Preferences.RINGTONE);
    ringPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
      @Override
      public boolean onPreferenceChange(Preference preference, Object newValue) {
        setNotificationSoundPreference((RingtonePreference) preference, Uri.parse((String) newValue));
        return true;
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (key.equals(Preferences.AUTO_SYNC)) {
      if (sharedPreferences.getBoolean(key, true)) {
        SyncService.scheduleSync(this);
      } else {
        SyncService.unscheduleSync(this);
      }
    } else if (key.equals(Preferences.REFRESH_INTERVAL)) {
      setRefreshIntervalPreference();
      SyncService.scheduleSync(this);
    }

    if (backupManagerAvailable) {
      BackupManagerWrapper.dataChanged(getPackageName());
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        UIUtils.goHome(this);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
    }

  private void setRefreshIntervalPreference() {
    ListPreference interval = (ListPreference) findPreference(Preferences.REFRESH_INTERVAL);
    interval.setSummary(String.format(getString(R.string.pref_refresh_interval_summary), interval.getEntry()));
  }

  private void setNotificationSoundPreference() {
    RingtonePreference preference = (RingtonePreference) findPreference(Preferences.RINGTONE);
    Uri uri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString(preference.getKey(), ""));

    setNotificationSoundPreference(preference, uri);
  }

  private void setNotificationSoundPreference(RingtonePreference preference, Uri uri) {
    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
    preference.setSummary(ringtone != null ? ringtone.getTitle(this) : getString(R.string.pref_ringtone_summary));
  }

  private void setAboutPreference() {
    findPreference(Preferences.BLOG).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.blog_url));
        return true;
      }
    });
    findPreference(Preferences.FACEBOOK).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.facebook_url));
        return true;
      }
    });
    findPreference(Preferences.FEEDBACK).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.feedback_url));
        return true;
      }
    });
  }

  private void setVersionPreference() {
    Preference pref = findPreference(Preferences.VERSION);
    try {
      String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      pref.setTitle(String.format(getString(R.string.pref_version_title), getString(R.string.app_name), version));
    } catch (NameNotFoundException e) {
      pref.setTitle(getString(R.string.app_name));
    }
    pref.setSummary(String.format(getString(R.string.pref_version_summary), new Date().getYear()+1900));
    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openMarket(PreferencesActivity.this);
        return true;
      }
    });
  }

  public static final class Preferences {
    public static final String ON_BOOT = "onBoot";
    public static final String AUTO_SYNC = "autoSync";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SYNC_FAVORITES_ONLY = "syncFavoritesOnly";
    public static final String DONT_SYNC_DELIVERED_ITEMS = "dontSyncDeliveredItems";
    public static final String NOTIFY = "notify";
    public static final String NOTIFY_SYNC = "notifySync";
    public static final String NOTIFY_ALL = "notifyAll";
    public static final String NOTIFY_RETURNED = "notifyReturned";
    public static final String NOTIFY_UNKNOWN = "notifyUnknown";
    public static final String NOTIFY_IRREGULAR = "notifyIrregular";
    public static final String NOTIFY_FAVORITES = "notifyFavorites";
    public static final String NOTIFY_AVAILABLE = "notifyAvailable";
    public static final String NOTIFY_DELIVERED = "notifyDelivered";
    public static final String RINGTONE = "ringtone";
    public static final String LIGHTS = "lights";
    public static final String VIBRATE = "vibrate";
    public static final String BLOG = "blog";
    public static final String FACEBOOK = "facebook";
    public static final String FEEDBACK = "feedback";
    public static final String VERSION = "version";
  }
}
