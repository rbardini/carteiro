package com.rbardini.carteiro.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public class SearchActivity extends Activity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  private CarteiroApplication app;
  private ActionBar actionBar;
  private PostalListFragment listFragment;
  private String query;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) UIUtils.addStatusBarPadding(this, R.id.root_layout, true);

    app = (CarteiroApplication) getApplication();

    actionBar = getActionBar();
    actionBar.setTitle(R.string.subtitle_search);
    listFragment = null;
    query = null;

    handleIntent();
  }

  @Override
  public void onResume() {
    super.onResume();

    CarteiroApplication.state.receiver.setReceiver(this);
    updateRefreshStatus();
  }

  @Override
  protected void onPause() {
    super.onPause();

    CarteiroApplication.state.receiver.clearReceiver();
  }

  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    switch (resultCode) {
    case SyncService.STATUS_RUNNING: {
      updateRefreshStatus();
      break;
    }
    case SyncService.STATUS_FINISHED: {
      updateRefreshStatus();
      if (((CarteiroApplication) getApplication()).hasUpdate()) {
        listFragment.refreshList(false);
      }
      break;
    }
    case SyncService.STATUS_ERROR: {
      updateRefreshStatus();
      final String error = getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
      UIUtils.showToast(this, error);
      break;
    }
  }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.search_actions, menu);

    MenuItem searchViewButton = menu.findItem(R.id.search_view_opt);
    if (searchViewButton != null) {
      SearchView searchView = (SearchView) searchViewButton.getActionView();
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);

    String toast;
    if (desc == null) toast = getString(R.string.toast_item_renamed_empty, pi.getCod());
    else toast = getString(R.string.toast_item_renamed, pi.getSafeDesc(), desc);

    UIUtils.showToast(this, toast);

    listFragment.clearSelection();
    listFragment.refreshList(true);
  }

  @Override
  public void onDeletePostalItems(ArrayList<PostalItem> piList) {
    final int listSize = piList.size();

    for (PostalItem pi : piList) app.getDatabaseHelper().deletePostalItem(pi.getCod());

    String message = listSize == 1
      ? getString(R.string.toast_item_deleted, piList.get(0).getSafeDesc())
      : getString(R.string.toast_items_deleted, listSize);
    UIUtils.showToast(this, message);

    listFragment.clearSelection();
    listFragment.refreshList(true);
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    listFragment.refreshList(true);
  }

  private void handleIntent() {
    Intent intent = getIntent();
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      query = intent.getStringExtra(SearchManager.QUERY);
      actionBar.setSubtitle(query);
      if (listFragment == null) {
        listFragment = PostalListFragment.newInstance(query);
        getFragmentManager().beginTransaction().replace(R.id.search_list, listFragment).commit();
      } else {
        listFragment.setQuery(query);
        listFragment.refreshList(false);
      }
    } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
      PostalItem pi = app.getDatabaseHelper().getPostalItem(intent.getDataString());
      Intent record = new Intent(this, RecordActivity.class);
      record.putExtra("postalItem", pi);
      startActivity(record);
      finish();
    }
  }

  private void updateRefreshStatus() {
    if (CarteiroApplication.state.syncing) {
      listFragment.setRefreshing();
    } else {
      listFragment.onRefreshComplete();
    }
  }
}
