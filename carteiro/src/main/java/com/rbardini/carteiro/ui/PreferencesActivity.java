package com.rbardini.carteiro.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.Html;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.BackupManagerWrapper;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.IOUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener {
  private static boolean backupManagerAvailable;
  private static String unknownRingtoneTitle;

  private CarteiroApplication app;
  private DatabaseHelper dh;

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

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();

    setRefreshIntervalPreference();
    setNotificationSoundPreference();
    setBackupPreferences();
    setAboutPreferences();

    Preference ringPref = findPreference(getString(R.string.pref_key_ringtone));
    if (ringPref != null) {
      ringPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
          setNotificationSoundPreference((RingtonePreference) preference, Uri.parse((String) newValue));
          return true;
        }
      });
    }
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

  private void setBackupPreferences() {
    Preference createPref = findPreference(getString(R.string.pref_key_create_backup));
    Preference restorePref = findPreference(getString(R.string.pref_key_restore_backup));

    if (createPref != null) {
      createPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          final View dialogView = getLayoutInflater().inflate(R.layout.backup_dialog, null);
          final EditText backupNameField = (EditText) dialogView.findViewById(R.id.backup_name);
          final Context context = PreferencesActivity.this;
          final String[] currentName = DatabaseHelper.DB_NAME.split("\\.");

          // Add current timestamp to the suggested backup filename to avoid collision
          backupNameField.append(currentName[0] + '-' + IOUtils.SAFE_DATE_FORMAT.format(new Date()) + '.' + currentName[1]);

          new AlertDialog.Builder(context)
              .setTitle(R.string.pref_create_backup_title)
              .setView(dialogView)
              .setPositiveButton(R.string.backup_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  File destDir = IOUtils.getExternalStoragePublicAppDocumentsDirectory();
                  String backupName = backupNameField.getText().toString();
                  String backupResult = null;

                  // Add database file extension to the backup filename in case the user has removed it
                  String dbExt = getDatabaseFileExtension();
                  if (!backupName.endsWith(dbExt)) backupName += dbExt;

                  File backupFile = IOUtils.createFile(destDir, backupName);

                  try {
                    backupFile = dh.exportDatabase(getApplicationContext(), backupFile);
                    backupResult = backupFile == null ? getString(R.string.toast_backup_creation_fail, getString(R.string.toast_external_storage_write_error))
                                                      : getString(R.string.toast_backup_created, destDir.getParentFile().getName());
                  } catch (Exception e) {
                    backupResult = getString(R.string.toast_backup_creation_fail, e.getMessage());
                  } finally {
                    UIUtils.showToast(context, backupResult);
                  }
                }
              })
              .setNegativeButton(R.string.negative_btn, null)
              .show();

          return true;
        }
      });
    }

    if (restorePref != null) {
      restorePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        final Context context = PreferencesActivity.this;

        @Override
        public boolean onPreferenceClick(Preference preference) {
          if (!IOUtils.isExternalStorageReadable()) {
            UIUtils.showToast(context, R.string.toast_external_storage_read_error);
            return true;
          }

          final File documentsDir = IOUtils.getExternalStoragePublicAppDocumentsDirectory();
          final String[] backupFiles = documentsDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
              return filename.endsWith(getDatabaseFileExtension());
            }
          });

          if (backupFiles == null || backupFiles.length < 1) {
            UIUtils.showToast(context, R.string.toast_backup_not_found);
            return true;
          }

          new AlertDialog.Builder(context)
              .setTitle(R.string.title_alert_select_backup)
              .setItems(backupFiles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  final String backupName = backupFiles[which];

                  new AlertDialog.Builder(context)
                      .setIcon(android.R.drawable.ic_dialog_alert)
                      .setTitle(R.string.title_alert_restore_backup)
                      .setMessage(Html.fromHtml(getString(R.string.msg_alert_restore_backup, backupName)))
                      .setPositiveButton(R.string.restore_btn, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                          File backupFile = IOUtils.createFile(documentsDir, backupName);
                          String restoreResult = null;

                          try {
                            backupFile = dh.importDatabase(context, backupFile);

                            if (backupFile == null) {
                              restoreResult = getString(R.string.toast_backup_restore_fail, getString(R.string.toast_external_storage_read_error));
                            } else {
                              restoreResult = getString(R.string.toast_backup_restored);
                              app.setUpdatedList();
                            }
                          } catch (Exception e) {
                            restoreResult = getString(R.string.toast_backup_restore_fail, e.getMessage());
                          } finally {
                            UIUtils.showToast(context, restoreResult);
                          }
                        }
                      })
                      .setNegativeButton(R.string.negative_btn, null)
                      .show();
                }
              })
              .show();

          return true;
        }
      });
    }
  }

  private void setAboutPreferences() {
    Preference aboutPref = findPreference(getString(R.string.pref_key_about));
    Preference ratePref = findPreference(getString(R.string.pref_key_rate));

    if (aboutPref != null) {
      try {
        String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        aboutPref.setTitle(String.format(getString(R.string.pref_about_title), getString(R.string.app_name), version));
      } catch (NameNotFoundException e) {
        aboutPref.setTitle(getString(R.string.app_name));
      }
      aboutPref.setSummary(String.format(getString(R.string.pref_about_summary), new Date().getYear() + 1900));
    }

    if (ratePref != null) {
      ratePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          UIUtils.openMarket(PreferencesActivity.this);
          return true;
        }
      });
    }
  }

  private String getDatabaseFileExtension() {
    String dbName = DatabaseHelper.DB_NAME;
    return dbName.substring(dbName.lastIndexOf("."));
  }
}
