package com.rbardini.carteiro.ui;

import java.util.Date;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
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
  private static String unknownRingtoneTitle;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    unknownRingtoneTitle = getString(Resources.getSystem().getIdentifier("ringtone_unknown", "string", "android"));

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

    Preference ringPref = findPreference(getString(R.string.pref_key_ringtone));
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
    if (key.equals(getString(R.string.pref_key_auto_sync))) {
      if (sharedPreferences.getBoolean(key, true)) {
        SyncService.scheduleSync(this);
      } else {
        SyncService.unscheduleSync(this);
      }
    } else if (key.equals(getString(R.string.pref_key_refresh_interval))) {
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
    ListPreference interval = (ListPreference) findPreference(getString(R.string.pref_key_refresh_interval));
    interval.setSummary(String.format(getString(R.string.pref_refresh_interval_summary), interval.getEntry()));
  }

  private void setNotificationSoundPreference() {
    RingtonePreference preference = (RingtonePreference) findPreference(getString(R.string.pref_key_ringtone));
    Uri uri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(this).getString(preference.getKey(), ""));

    setNotificationSoundPreference(preference, uri);
  }

  private void setNotificationSoundPreference(RingtonePreference preference, Uri uri) {
    Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
    String ringtoneTitle = null;

    if (ringtone != null) ringtoneTitle = ringtone.getTitle(this);
    if (ringtoneTitle == null || ringtoneTitle.equals(unknownRingtoneTitle)) ringtoneTitle = getString(R.string.pref_ringtone_summary);

    preference.setSummary(ringtoneTitle);
  }

  private void setAboutPreference() {
    findPreference(getString(R.string.pref_key_blog)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.blog_url));
        return true;
      }
    });
    findPreference(getString(R.string.pref_key_facebook)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.facebook_url));
        return true;
      }
    });
    findPreference(getString(R.string.pref_key_twitter)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.twitter_url));
        return true;
      }
    });
    findPreference(getString(R.string.pref_key_gplus)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.gplus_url));
        return true;
      }
    });
    findPreference(getString(R.string.pref_key_feedback)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openURL(PreferencesActivity.this, getString(R.string.feedback_url));
        return true;
      }
    });
    findPreference(getString(R.string.pref_key_rate)).setOnPreferenceClickListener(new OnPreferenceClickListener() {
      @Override
      public boolean onPreferenceClick(Preference preference) {
        UIUtils.openMarket(PreferencesActivity.this);
        return true;
      }
    });
  }

  private void setVersionPreference() {
    Preference pref = findPreference(getString(R.string.pref_key_version));
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
}
