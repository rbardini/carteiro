package com.rbardini.carteiro.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.rbardini.carteiro.R;

public class PreferencesActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_preferences);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      PreferencesFragment prefsFragment = PreferencesFragment.newInstance();
      getFragmentManager().beginTransaction().replace(R.id.main_content, prefsFragment, PreferencesFragment.TAG).commit();
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
