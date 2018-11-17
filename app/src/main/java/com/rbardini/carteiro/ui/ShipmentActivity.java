package com.rbardini.carteiro.ui;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncTask;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class ShipmentActivity extends AppCompatActivity implements ShipmentListFragment.OnPostalListActionListener, ShipmentRenameDialogFragment.OnShipmentRenameListener {
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
    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(SyncTask.ACTION_SYNC));
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
    int status = intent.getIntExtra(SyncTask.EXTRA_STATUS, SyncTask.STATUS_FINISHED);

    switch (status) {
      case SyncTask.STATUS_RUNNING:
        updateRefreshStatus();
        break;

      case SyncTask.STATUS_FINISHED:
        updateRefreshStatus();
        refreshList();
        break;

      case SyncTask.STATUS_ERROR:
        updateRefreshStatus();

        Snackbar snackbar = Snackbar
          .make(findViewById(R.id.content), R.string.toast_net_error, Snackbar.LENGTH_LONG)
          .setAction(R.string.check_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              UIUtils.openURL(ShipmentActivity.this, PostalUtils.HEALTH_URL);
            }
          });
        snackbar.show();

        Log.e(TAG, intent.getStringExtra(SyncTask.EXTRA_ERROR));
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
