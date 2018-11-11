package com.rbardini.carteiro.ui;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.rbardini.carteiro.svc.BackupManagerWrapper;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public abstract class PreferencesFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {
  private static boolean mIsBackupManagerAvailable;

  abstract int getTitleId();

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

    getActivity().setTitle(getTitleId());
    PreferenceManager
      .getDefaultSharedPreferences(getActivity())
      .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();

    PreferenceManager
      .getDefaultSharedPreferences(getActivity())
      .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (mIsBackupManagerAvailable) {
      BackupManagerWrapper.dataChanged(getActivity().getApplication().getPackageName());
    }
  }
}
