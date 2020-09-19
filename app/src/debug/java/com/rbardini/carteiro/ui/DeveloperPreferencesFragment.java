package com.rbardini.carteiro.ui;

import android.os.Bundle;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.Developer;

import androidx.preference.Preference;

public class DeveloperPreferencesFragment extends PreferencesFragment {
  @Override
  int getTitleId() {
    return R.string.pref_developer_title;
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences_developer, rootKey);

    setupPopulateListPreference();
    setupNotifyUpdatePreference();
  }

  private void setupPopulateListPreference() {
    Preference pref = findPreference(getString(R.string.pref_key_populate_list));

    if (pref != null) {
      pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          Developer.populate(((CarteiroApplication) getActivity().getApplication()).getDatabaseHelper());
          return true;
        }
      });
    }
  }

  private void setupNotifyUpdatePreference() {
    Preference pref = findPreference(getString(R.string.pref_key_notify_update));

    if (pref != null) {
      pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          Developer.sendNotification(getActivity());
          return true;
        }
      });
    }
  }
}
