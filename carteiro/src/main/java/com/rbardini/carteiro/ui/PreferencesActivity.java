package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class PreferencesActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      PreferencesFragment prefsFragment = PreferencesFragment.newInstance();
      getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment, PreferencesFragment.TAG).commit();
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
}
