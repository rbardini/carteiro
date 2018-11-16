package com.rbardini.carteiro.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.util.Constants;
import com.rbardini.carteiro.util.IOUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;
import com.takisoft.preferencex.RingtonePreference;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

public class PreferencesActivity extends AppCompatActivity implements OnPreferenceStartFragmentCallback {
  private static CarteiroApplication app;
  private static DatabaseHelper dh;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preferences);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();

    showFragment(getCurrentFragment(savedInstanceState), false);
    handleIntent();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    switch (requestCode) {
      case Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
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
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    final Fragment fragment = Fragment.instantiate(this, pref.getFragment(), pref.getExtras());
    showFragment(fragment, true);

    return true;
  }

  private Fragment getCurrentFragment(Bundle savedInstanceState) {
    return savedInstanceState == null ?
      new MainPreferences() : getSupportFragmentManager().findFragmentById(R.id.main_content);
  }

  private void showFragment(Fragment fragment, boolean addToBackStack) {
    FragmentTransaction ft = getSupportFragmentManager()
      .beginTransaction()
      .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
      .replace(R.id.main_content, fragment);

    if (addToBackStack) {
      ft.addToBackStack(null);
    }

    ft.commit();
  }

  private void handleIntent() {
    Intent intent = getIntent();
    if (intent == null) return;

    Set<String> categories = intent.getCategories();
    if (categories == null || categories.isEmpty()) return;

    if (categories.contains("android.intent.category.NOTIFICATION_PREFERENCES")) {
      showFragment(new NotificationPreferences(), false);
    }
  }

  public static class MainPreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.title_preferences;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences, rootKey);

      TypedArray attrs = getActivity().getTheme().obtainStyledAttributes(new int[] {android.R.attr.textColorSecondary});
      PreferenceGroup group = getPreferenceScreen();
      for (int i = 0; i < group.getPreferenceCount(); i++) {
        group.getPreference(i).getIcon().mutate().setColorFilter(attrs.getColor(0, Color.TRANSPARENT), PorterDuff.Mode.SRC_ATOP);
      }
    }
  }

  public static class SyncingPreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.pref_syncing_title;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_syncing, rootKey);
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
    @Override
    int getTitleId() {
      return R.string.pref_notification_title;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_notification, rootKey);
      addPreferencesFromResource(R.xml.preferences_notification_indication);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        setNotificationSettingsPreference();

      } else {
        setNotificationSoundPreference();
      }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setNotificationSettingsPreference() {
      Preference pref = findPreference(getString(R.string.pref_key_notification_settings));
      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, app.getPackageName());
            startActivity(intent);
            return true;
          }
        });
      }
    }

    private void setNotificationSoundPreference() {
      Preference pref = findPreference(getString(R.string.pref_key_ringtone));
      if (pref != null) {
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
          @Override
          public boolean onPreferenceChange(Preference preference, Object newValue) {
            updateNotificationSoundPreference((RingtonePreference) preference, (Uri) newValue);
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

      preference.setSummary(ringtoneTitle);
    }
  }

  public static class AppearancePreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.pref_appearance_title;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_appearance, rootKey);

      setupInitialCategoryPreference();
      setThemePreference();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      super.onSharedPreferenceChanged(sharedPreferences, key);

      if (key.equals(getString(R.string.pref_key_initial_category))) {
        setInitialCategoryPreference();
      }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
      if (preference instanceof ThemePreference) {
        Bundle bundle = new Bundle(1);
        bundle.putString("key", preference.getKey());

        DialogFragment dialogFragment = new ThemePreference.ThemePreferenceDialogFragmentCompat();
        dialogFragment.setArguments(bundle);
        dialogFragment.setTargetFragment(this, 0);
        dialogFragment.show(getFragmentManager(), "android.support.v7.preference.PreferenceFragment.DIALOG");
      } else {
        super.onDisplayPreferenceDialog(preference);
      }
    }

    private void setupInitialCategoryPreference() {
      ListPreference pref = (ListPreference) findPreference(getString(R.string.pref_key_initial_category));
      Map<Integer, Integer> titleMap = Category.getTitleMap();

      CharSequence[] entries = new CharSequence[titleMap.size()];
      CharSequence[] entryValues = new CharSequence[titleMap.size()];
      int index = 0;

      for (Entry<Integer, Integer> entry : Category.getTitleMap().entrySet()) {
        entries[index] = getString(entry.getValue());
        entryValues[index] = String.valueOf(entry.getKey());
        index++;
      }

      pref.setEntries(entries);
      pref.setEntryValues(entryValues);

      setInitialCategoryPreference();
    }

    private void setInitialCategoryPreference() {
      ListPreference pref = (ListPreference) findPreference(getString(R.string.pref_key_initial_category));
      pref.setSummary(pref.getEntry());
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
    int getTitleId() {
      return R.string.pref_backup_title;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_backup, rootKey);

      checkRequiredPermissions();
      setupCreatePreference();
      setupRestorePreference();
    }

    private void checkRequiredPermissions() {
      String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

      if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
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
            final EditText backupNameField = dialogView.findViewById(R.id.backup_name);
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
    int getTitleId() {
      return R.string.pref_about_title;
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_about, rootKey);

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
