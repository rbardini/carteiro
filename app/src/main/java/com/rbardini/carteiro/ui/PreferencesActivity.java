package com.rbardini.carteiro.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.MenuItem;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncScheduler;
import com.rbardini.carteiro.util.AnalyticsUtils;
import com.rbardini.carteiro.util.Constants;
import com.rbardini.carteiro.util.IOUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceFragmentCompat.OnPreferenceStartFragmentCallback;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

public class PreferencesActivity extends AppCompatActivity implements OnPreferenceStartFragmentCallback {
  private static final int PICK_CREATE_BACKUP_FILE_REQUEST = 1;
  private static final int PICK_RESTORE_BACKUP_FILE_REQUEST = 2;
  private static final String BACKUP_FILE_MIME_TYPE = "application/octet-stream";

  private static CarteiroApplication app;
  private static DatabaseHelper dh;

  private PreferencesFragment mCurrentFragment;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preferences);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    app = (CarteiroApplication) getApplication();
    dh = app.getDatabaseHelper();

    getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        mCurrentFragment = (PreferencesFragment) getSupportFragmentManager().findFragmentById(R.id.main_content);
        recordScreenView();
      }
    });

    showFragment(getCurrentFragment(savedInstanceState), false);
    handleIntent();
  }

  @Override
  public void onResume() {
    super.onResume();
    recordScreenView();
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

    mCurrentFragment = (PreferencesFragment) fragment;
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

  private void recordScreenView() {
    int title = mCurrentFragment.getTitleId();
    String screenName = UIUtils.getDefaultString(this, title);

    AnalyticsUtils.recordScreenView(this, screenName);
  }

  public static class MainPreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.title_preferences;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_notification, rootKey);
      addPreferencesFromResource(R.xml.preferences_notification_indication);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
      final String key = preference.getKey();

      if (key.equals(getString(R.string.pref_key_notification_settings))) {
        return showSystemNotificationSettings();
      }

      return super.onPreferenceTreeClick(preference);
    }

    private boolean showSystemNotificationSettings() {
      Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, app.getPackageName());

      startActivity(intent);
      return true;
    }
  }

  public static class AppearancePreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.pref_appearance_title;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_appearance, rootKey);

      setupInitialCategoryPreference();
      setThemePreference();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      super.onSharedPreferenceChanged(sharedPreferences, key);

      if (key.equals(getString(R.string.pref_key_initial_category))) {
        setInitialCategoryPreference();
        return;
      }

      if (key.equals(getString(R.string.pref_key_theme))) {
        setThemePreference();
      }
    }

    private void setupInitialCategoryPreference() {
      ListPreference pref = findPreference(getString(R.string.pref_key_initial_category));
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
      ListPreference pref = findPreference(getString(R.string.pref_key_initial_category));
      pref.setSummary(pref.getEntry());
    }

    private void setThemePreference() {
      ListPreference pref = findPreference(getString(R.string.pref_key_theme));
      String value = pref.getValue();

      pref.setSummary(value.equals(getString(R.string.theme_system))
        ? R.string.pref_theme_system
        : value.equals(getString(R.string.theme_dark)) ? R.string.pref_theme_dark : R.string.pref_theme_light
      );
      UIUtils.setTheme(getActivity(), value);
    }
  }

  public static class BackupPreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.pref_backup_title;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      setPreferencesFromResource(R.xml.preferences_backup, rootKey);

      checkRequiredPermissions();
      updateLastBackup();
      setupCreatePreference();
      setupRestorePreference();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
      super.onActivityResult(requestCode, resultCode, resultData);

      if (resultCode != Activity.RESULT_OK) return;

      final Context context = getActivity();
      final Uri uri = resultData.getData();

      switch (requestCode) {
        case PICK_CREATE_BACKUP_FILE_REQUEST:
          String createResult = null;

          try {
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream stream = new FileOutputStream(fd.getFileDescriptor());

            dh.exportDatabase(app.getApplicationContext(), stream);
            createResult = getString(R.string.toast_backup_created);
          } catch (Exception e) {
            createResult = getString(R.string.toast_backup_creation_fail, e.getMessage());
          } finally {
            UIUtils.showToast(context, createResult);
          }
          break;

        case PICK_RESTORE_BACKUP_FILE_REQUEST:
          new AlertDialog.Builder(context)
            .setTitle(R.string.title_alert_restore_backup)
            .setMessage(R.string.msg_alert_restore_backup)
            .setPositiveButton(R.string.restore_btn, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                String restoreResult = null;

                try {
                  ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "r");
                  FileInputStream stream = new FileInputStream(fd.getFileDescriptor());

                  dh.importDatabase(context, stream);
                  restoreResult = getString(R.string.toast_backup_restored);
                } catch (Exception e) {
                  restoreResult = getString(R.string.toast_backup_restore_fail, e.getMessage());
                } finally {
                  UIUtils.showToast(context, restoreResult);
                }
              }
            })
            .setNegativeButton(R.string.negative_btn, null)
            .show();
          break;
      }
    }

    private void updateLastBackup() {
      Preference pref = findPreference(getString(R.string.pref_key_last_backup));
      pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          return showSystemBackupSettings();
        }
      });

      long lastBackupTimestamp = PreferenceManager.getDefaultSharedPreferences(getActivity())
        .getLong(getString(R.string.pref_key_last_backup), 0);

      if (lastBackupTimestamp == 0) return;

      String lastBackupRelative = DateUtils.getRelativeDateTimeString(
        getActivity(),
        lastBackupTimestamp,
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        0
      ).toString();

      pref.setSummary(pref.getSummary() + "\n\n" + getString(R.string.pref_backup_last_notice, lastBackupRelative));
    }

    private void checkRequiredPermissions() {
      String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

      if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, Constants.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
      }
    }

    private void setupCreatePreference() {
      Preference pref = findPreference(getString(R.string.pref_key_create_backup));

      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            final String[] dbName = DatabaseHelper.DB_NAME.split("\\.");
            final String backupName = dbName[0] + '-' + IOUtils.SAFE_DATE_FORMAT.format(new Date()) + '.' + dbName[1];

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
              .addCategory(Intent.CATEGORY_OPENABLE)
              .setType(BACKUP_FILE_MIME_TYPE)
              .putExtra(Intent.EXTRA_TITLE, backupName);

            startActivityForResult(intent, PICK_CREATE_BACKUP_FILE_REQUEST);
            return true;
          }
        });
      }
    }

    private void setupRestorePreference() {
      Preference pref = findPreference(getString(R.string.pref_key_restore_backup));

      if (pref != null) {
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
          @Override
          public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT)
              .addCategory(Intent.CATEGORY_OPENABLE)
              .setType(BACKUP_FILE_MIME_TYPE);

            startActivityForResult(intent, PICK_RESTORE_BACKUP_FILE_REQUEST);
            UIUtils.showToast(getActivity(), R.string.toast_backup_select);
            return true;
          }
        });
      }
    }

    private boolean showSystemBackupSettings() {
      Intent intent = new Intent(Settings.ACTION_SETTINGS);

      startActivity(intent);
      return true;
    }
  }

  public static class AboutPreferences extends PreferencesFragment {
    @Override
    int getTitleId() {
      return R.string.pref_about_title;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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
