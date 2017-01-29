package com.rbardini.carteiro.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.rbardini.carteiro.svc.BackupManagerWrapper;

public class PreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
  private static boolean mIsBackupManagerAvailable;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      BackupManagerWrapper.checkAvailable();
      mIsBackupManagerAvailable = true;

    } catch (Throwable t) {
      mIsBackupManagerAvailable = false;
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    prefs.registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    prefs.unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (mIsBackupManagerAvailable) {
      BackupManagerWrapper.dataChanged(getActivity().getApplication().getPackageName());
    }
  }
}
