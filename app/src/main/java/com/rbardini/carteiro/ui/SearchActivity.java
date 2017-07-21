package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;

public class SearchActivity extends ShipmentActivity {
  private ActionBar mActionBar;
  private ShipmentListFragment mCurrentFragment;
  private String mQuery;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    setContentView(R.layout.activity_search);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    mActionBar = getSupportActionBar();
    mActionBar.setTitle(R.string.subtitle_search);
    mActionBar.setDisplayHomeAsUpEnabled(true);
    mQuery = null;

    handleIntent();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent();
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
        finish();
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

  private void handleIntent() {
    Intent intent = getIntent();
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      mQuery = intent.getStringExtra(SearchManager.QUERY);
      mActionBar.setSubtitle(mQuery);

      if (mCurrentFragment == null) {
        mCurrentFragment = ShipmentListFragment.newInstance(mQuery);
        getFragmentManager().beginTransaction().replace(R.id.content, mCurrentFragment).commit();

      } else {
        mCurrentFragment.setQuery(mQuery);
        refreshList();
      }

    } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      Shipment shipment = app.getDatabaseHelper().getShallowShipment(intent.getDataString());
      Intent recordIntent = new Intent(this, RecordActivity.class).putExtra("shipment", shipment);
      startActivity(recordIntent);
      finish();
    }
  }
}
