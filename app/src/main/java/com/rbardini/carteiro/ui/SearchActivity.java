package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.util.AnalyticsUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

public class SearchActivity extends ShipmentActivity {
  private ActionBar mActionBar;
  private ShipmentListFragment mCurrentFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    setContentView(R.layout.activity_search);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    mActionBar = getSupportActionBar();
    mActionBar.setSubtitle(R.string.subtitle_search);
    mActionBar.setDisplayHomeAsUpEnabled(true);

    handleIntent(savedInstanceState == null);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    setIntent(intent);
    handleIntent(true);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.search_actions, menu);
    return super.onCreateOptionsMenu(menu);
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
  public void finish() {
    super.finish();
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
  }

  @Override
  public ShipmentFragment getPostalFragment() {
    return mCurrentFragment;
  }

  private void handleIntent(boolean isNewSearch) {
    Intent intent = getIntent();

    switch (intent.getAction()) {
      case Intent.ACTION_SEARCH:
        String query = intent.getStringExtra(SearchManager.QUERY);
        mActionBar.setTitle(query);

        if (mCurrentFragment == null) {
          mCurrentFragment = ShipmentListFragment.newInstance(query);
          getSupportFragmentManager().beginTransaction().replace(R.id.content, mCurrentFragment).commit();

        } else {
          mCurrentFragment.setQuery(query);
          refreshList();
        }

        if (isNewSearch) {
          AnalyticsUtils.recordSearch(this, query);
        }
        break;

      case Intent.ACTION_VIEW:
        Shipment shipment = app.getDatabaseHelper().getShallowShipment(intent.getDataString());
        Intent recordIntent = new Intent(this, RecordActivity.class).putExtra(RecordActivity.EXTRA_SHIPMENT, shipment);
        startActivity(recordIntent);
        finish();
        break;
    }
  }
}
