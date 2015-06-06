package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public abstract class PostalActivity extends AppCompatActivity implements DetachableResultReceiver.Receiver, PostalListFragment.OnPostalListActionListener, PostalItemDialogFragment.OnPostalItemChangeListener {
  protected CarteiroApplication app;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    app = (CarteiroApplication) getApplication();
  }

  @Override
  protected void onResume() {
    super.onResume();
    CarteiroApplication.state.receiver.setReceiver(this);
    updateRefreshStatus();
  }

  @Override
  protected void onPause() {
    CarteiroApplication.state.receiver.clearReceiver();
    super.onPause();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem searchViewButton = menu.findItem(R.id.search_view_opt);
    if (searchViewButton != null) {
      SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewButton);
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public void onReceiveResult(int resultCode, Bundle resultData) {
    switch (resultCode) {
      case SyncService.STATUS_RUNNING:
        updateRefreshStatus();
        break;

      case SyncService.STATUS_FINISHED:
        updateRefreshStatus();
        if (app.hasUpdate()) refreshList();
        break;



      case SyncService.STATUS_ERROR:
        updateRefreshStatus();
        final String error = getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
        UIUtils.showToast(this, error);
        break;
    }
  }

  @Override
  public void onPostalListAttached(PostalListFragment f) {}

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);

    String toast;
    if (desc == null) toast = getString(R.string.toast_item_renamed_empty, pi.getCod());
    else toast = getString(R.string.toast_item_renamed, pi.getSafeDesc(), desc);
    UIUtils.showToast(this, toast);

    clearSelection();
    refreshList();

    app.setUpdatedList();
  }

  @Override
  public void onDeletePostalItems(ArrayList<PostalItem> piList) {
    final int listSize = piList.size();

    for (PostalItem pi : piList) app.getDatabaseHelper().deletePostalItem(pi.getCod());

    String message = listSize == 1
        ? getString(R.string.toast_item_deleted, piList.get(0).getSafeDesc())
        : getString(R.string.toast_items_deleted, listSize);
    UIUtils.showToast(this, message);

    clearSelection();
    refreshList();

    // Calling CarteiroApplication.setUpdatedList here is unnecessary,
    // as the PostalListFragment class already does it when (un)archived
    // or deleted item animations are finished
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    refreshList();  // TODO Don't refresh the whole list, just update the item acted on

    app.setUpdatedList();
  }

  public void addStatusBarPadding() {
    if (getResources().getBoolean(R.bool.translucent_status)) {
      UIUtils.addStatusBarPadding(this, R.id.root_layout);
    }
  }

  public void updateRefreshStatus() {
    PostalFragment f = getPostalFragment();
    if (f == null) return;

    if (CarteiroApplication.state.syncing) {
      f.setRefreshing();
    } else {
      f.onRefreshComplete();
    }
  }

  public void refreshList() {
    PostalFragment f = getPostalFragment();
    if (f == null) return;

    f.refreshList();
  }

  public void clearSelection() {
    PostalFragment f = getPostalFragment();
    if (f == null) return;

    f.clearSelection();
  }

  public abstract PostalFragment getPostalFragment();
}
