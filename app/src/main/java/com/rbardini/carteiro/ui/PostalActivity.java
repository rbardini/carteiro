package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public abstract class PostalActivity extends AppCompatActivity implements DetachableResultReceiver.Receiver, PostalListFragment.OnPostalListActionListener, PostalItemDialogFragment.OnPostalItemChangeListener {
  private static final String TAG = "PostalActivity";
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

        Snackbar snackbar = Snackbar
          .make(findViewById(R.id.content), R.string.toast_net_error, Snackbar.LENGTH_LONG)
          .setAction(R.string.status_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              UIUtils.openURL(PostalActivity.this, PostalUtils.HEALTH_URL);
            }
          })
          .setActionTextColor(ContextCompat.getColor(this, R.color.error_foreground));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.error_background));
        snackbar.show();

        Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
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
    final DatabaseHelper dh = app.getDatabaseHelper();

    for (PostalItem pi : piList) {
      PostalItemRecord pir = new PostalItemRecord(pi);
      pir.deleteFrom(dh);
    }

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

  public void updateRefreshStatus() {
    PostalFragment f = getPostalFragment();
    if (f == null) return;

    f.updateRefreshStatus();
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
