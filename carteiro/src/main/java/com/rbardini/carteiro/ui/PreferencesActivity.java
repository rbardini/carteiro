package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;

public class PreferencesActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    FragmentManager fragManager = getFragmentManager();

    if (savedInstanceState == null) {
      PreferencesFragment prefsFragment = PreferencesFragment.newInstance();
      fragManager.beginTransaction().replace(android.R.id.content, prefsFragment, PreferencesFragment.TAG).commit();
    }
  }
}
