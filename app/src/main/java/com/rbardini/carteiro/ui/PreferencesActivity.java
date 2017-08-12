package com.rbardini.carteiro.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.util.IOUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.rbardini.carteiro.util.Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

public class PreferencesActivity extends AppCompatPreferenceActivity {
  private static CarteiroApplication app;
  private static DatabaseHelper dh;

  private static final List<String> fragments = new ArrayList<>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ViewGroup root = (ViewGroup) findViewById(android.R.id.list).getParent().getParent().getParent();
    Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.action_bar, root, false);
    root.addView(toolbar, 0);

    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();
  }

  @Override
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);

    fragments.clear();
    for (Header header : target) {
      fragments.add(header.fragment);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
        if (grantResults.length == 0) {
          onBackPressed();
          return;
        }

        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
          UIUtils.showToast(this, R.string.pref_backup_permission_denied);
          onBackPressed();
        }
      }
    }
  }

  @Override
  protected boolean isValidFragment(String fragmentName) {
    return fragments.contains(fragmentName);
  }

  public static class SyncingPreferences extends PreferencesFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_syncing);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      super.onSharedPreferenceChanged(sharedPreferences, key);

      if (key.equals(getString(R.string.pref_key_auto_sync))) {
        if (sharedPreferences.getBoolean(key, true)) {
          SyncScheduler.schedule(getActivity());

        } else {
          SyncScheduler.unschedule(getActivity());
        }

        return;
      }

      if (key.equals(getString(R.string.pref_key_sync_wifi_only))) {
        SyncScheduler.schedule(getActivity());
      }
    }
  }

  public static class NotificationPreferences extends PreferencesFragment {
    private static String unknownRingtoneTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_notification);

      setNotificationSoundPreference();
    }

    private void setNotificationSoundPreference() {
      unknownRingtoneTitle = getString(Resources.getSystem().getIdentifier("ringtone_unknown", "string", "android"));

      Preference pref = findPreference(getString(R.string.pref_key_ringtone));
      if (pref != null) {
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            updateNotificationSoundPreference((RingtonePreference) preference, Uri.parse((String) newValue));
            return true;
          }
        });
      }

      updateNotificationSoundPreference();
    }

    private void updateNotificationSoundPreference() {
      RingtonePreference pref = (RingtonePreference) findPreference(getString(R.string.pref_key_ringtone));
      Uri uri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(pref.getKey(), ""));

      updateNotificationSoundPreference(pref, uri);
    }

    private void updateNotificationSoundPreference(RingtonePreference preference, Uri uri) {
      Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
      String ringtoneTitle = null;

      if (ringtone != null) ringtoneTitle = ringtone.getTitle(getActivity());
      if (ringtoneTitle == null || ringtoneTitle.equals(unknownRingtoneTitle)) ringtoneTitle = getString(R.string.pref_ringtone_summary);

      preference.setSummary(ringtoneTitle);
    }
  }

  public static class AppearancePreferences extends PreferencesFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_appearance);

      setThemePreference();
    }

    private void setThemePreference() {
      ThemePreference pref = (ThemePreference) findPreference(getString(R.string.pref_key_theme));

      if (pref.getValue().equals(getString(R.string.theme_dark))) {
        pref.setSummary(R.string.pref_theme_dark);

      } else {
        pref.setSummary(R.string.pref_theme_light);
      }
    }
  }

  public static class BackupPreferences extends PreferencesFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_backup);

      checkRequiredPermissions();
      setupCreatePreference();
      setupRestorePreference();
    }

    private void checkRequiredPermissions() {
      String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

      if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
      }
    }

    private String getDatabaseFileExtension() {
      String dbName = DatabaseHelper.DB_NAME;
      return dbName.substring(dbName.lastIndexOf("."));
    }

    private void setupCreatePreference() {
      Preference pref = findPreference(getString(R.string.pref_key_create_backup));

      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            final View dialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_backup, null);
            final EditText backupNameField = (EditText) dialogView.findViewById(R.id.backup_name);
            final Context context = getActivity();
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
                    backupFile = dh.exportDatabase(app.getApplicationContext(), backupFile);
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
    }

    private void setupRestorePreference() {
      Preference pref = findPreference(getString(R.string.pref_key_restore_backup));

      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          final Context context = getActivity();

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
  }

  public static class AboutPreferences extends PreferencesFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_about);

      setupRatePreference();
      setupVersionPreference();
    }

    private void setupRatePreference() {
      Preference pref = findPreference(getString(R.string.pref_key_rate));

      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            UIUtils.openMarket(getActivity());
            return true;
          }
        });
      }
    }

    private void setupVersionPreference() {
      Preference pref = findPreference(getString(R.string.pref_key_version));

      if (pref != null) {
        try {
          String version = app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
          pref.setTitle(String.format(getString(R.string.pref_version_title), getString(R.string.app_name), version));
        } catch (PackageManager.NameNotFoundException e) {
          pref.setTitle(getString(R.string.app_name));
        }
        pref.setSummary(String.format(getString(R.string.pref_version_summary), new Date().getYear() + 1900));
      }
    }
  }
}
