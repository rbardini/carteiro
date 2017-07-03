package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public abstract class ShipmentActivity extends AppCompatActivity implements ShipmentListFragment.OnPostalListActionListener, ShipmentDialogFragment.OnShipmentChangeListener {
  private static final String TAG = "ShipmentActivity";

  protected CarteiroApplication app;
  private BroadcastReceiver mReceiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    app = (CarteiroApplication) getApplication();
    mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        onSyncStatusChange(intent);
      }
    };
  }

  @Override
  protected void onResume() {
    super.onResume();
    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(SyncService.ACTION_SYNC));
    updateRefreshStatus();
  }

  @Override
  protected void onPause() {
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
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

  public void onSyncStatusChange(Intent intent) {
    int status = intent.getIntExtra(SyncService.EXTRA_STATUS, SyncService.STATUS_FINISHED);

    switch (status) {
      case SyncService.STATUS_RUNNING:
        updateRefreshStatus();
        break;

      case SyncService.STATUS_FINISHED:
        updateRefreshStatus();
        refreshList();
        break;

      case SyncService.STATUS_ERROR:
        updateRefreshStatus();

        Snackbar snackbar = Snackbar
          .make(findViewById(R.id.content), R.string.toast_net_error, Snackbar.LENGTH_LONG)
          .setAction(R.string.status_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              UIUtils.openURL(ShipmentActivity.this, PostalUtils.HEALTH_URL);
            }
          })
          .setActionTextColor(ContextCompat.getColor(this, R.color.error_foreground));
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.error_background));
        snackbar.show();

        Log.e(TAG, intent.getStringExtra(SyncService.EXTRA_ERROR));
        break;
    }
  }

  @Override
  public void onPostalListAttached(ShipmentListFragment f) {}

  @Override
  public void onRenameShipment(String desc, Shipment shipment) {
    app.getDatabaseHelper().renamePostalItem(shipment.getNumber(), desc);

    String toast;
    if (desc == null) toast = getString(R.string.toast_item_renamed_empty, shipment.getNumber());
    else toast = getString(R.string.toast_item_renamed, shipment.getDescription(), desc);
    UIUtils.showToast(this, toast);

    clearSelection();
    refreshList();
  }

  @Override
  public void onDeleteShipments(ArrayList<Shipment> shipments) {
    final int listSize = shipments.size();
    final DatabaseHelper dh = app.getDatabaseHelper();

    for (Shipment shipment : shipments) {
      dh.deletePostalItem(shipment.getNumber());
    }

    String message = listSize == 1
        ? getString(R.string.toast_item_deleted, shipments.get(0).getDescription())
        : getString(R.string.toast_items_deleted, listSize);
    UIUtils.showToast(this, message);

    clearSelection();
    refreshList();
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    refreshList();  // TODO Don't refresh the whole list, just update the item acted on
  }

  public void updateRefreshStatus() {
    ShipmentFragment f = getPostalFragment();
    if (f == null) return;

    f.updateRefreshStatus();
  }

  public void refreshList() {
    ShipmentFragment f = getPostalFragment();
    if (f == null) return;

    f.refreshList();
  }

  public void clearSelection() {
    ShipmentFragment f = getPostalFragment();
    if (f == null) return;

    f.clearSelection();
  }

  public abstract ShipmentFragment getPostalFragment();
}
