package com.rbardini.carteiro.ui;

import java.util.Date;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;

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

    setAboutPreference();
  }

  @Override
  protected void onResume() {
    super.onResume();

    PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    setRefreshIntervalPreference();
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

  private void setAboutPreference() {
    Preference about = findPreference(Preferences.ABOUT);
    try {
      String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      about.setTitle(String.format(getString(R.string.pref_about_title), getString(R.string.app_name), version));
    } catch (NameNotFoundException e) {
      about.setTitle(getString(R.string.app_name));
    }
    about.setSummary(String.format(getString(R.string.pref_about_summary), new Date().getYear()+1900));
    about.setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.app_url));
        return true;
      }
    });
  }

  private void setRefreshIntervalPreference() {
    ListPreference interval = (ListPreference) findPreference(Preferences.REFRESH_INTERVAL);
    interval.setSummary(String.format(getString(R.string.pref_refresh_interval_summary), interval.getEntry()));
  }

  public static final class Preferences {
    public static final String ON_BOOT = "onBoot";
    public static final String AUTO_SYNC = "autoSync";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SYNC_FAVORITES_ONLY = "syncFavoritesOnly";
    public static final String DONT_SYNC_DELIVERED_ITEMS = "dontSyncDeliveredItems";
    public static final String NOTIFY = "notify";
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
    public static final String ABOUT = "about";
  }
}
