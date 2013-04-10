package com.rbardini.carteiro.ui;

import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

public class RecordActivity extends SherlockFragmentActivity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  private CarteiroApplication app;
  private ActionBar actionBar;

  private PostalItem pi;

  private PostalRecordFragment recordFragment;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

        setContentView(R.layout.record);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    registerForContextMenu(findViewById(R.id.hidden_edit_opt));

        app = (CarteiroApplication) getApplication();
    actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);

    handleIntent();
  }

  @Override
  protected void onResume() {
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
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent();
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
        if (app.hasUpdate() && app.isUpdatedCod(pi.getCod())) {
          recordFragment.refreshList();
        }
        break;
      }
      case SyncService.STATUS_ERROR: {
        updateRefreshStatus();
        final String error = getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
        UIUtils.showToast(RecordActivity.this, error);
        break;
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.record_actions, menu);

    if (pi.isFav()) {
      menu.findItem(R.id.fav_opt).setIcon(R.drawable.ic_action_star);
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        UIUtils.goHome(this);
        return true;

      case R.id.fav_opt:
        app.getDatabaseHelper().togglePostalItemFav(pi.getCod());
        item.setIcon(pi.toggleFav() ? R.drawable.ic_action_star : R.drawable.ic_action_star_off);
        app.setUpdatedList();
        return true;

      case R.id.place_opt:
        if (pi.getLoc() == null) {
          UIUtils.showToast(this, getString(R.string.text_unknown_location));
        } else {
          Intent place = new Intent(Intent.ACTION_VIEW, Uri.parse(PostalUtils.getLocation(pi, true)));
          try {
            startActivity(place);
          } catch (Exception e) {
            UIUtils.showToast(this, e.getMessage());
          }
        }
        return true;

      case R.id.edit_opt:
        openContextMenu(findViewById(R.id.hidden_edit_opt));
        return true;

      case R.id.share_opt:
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_status_subject));
        share.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_status_text),
            pi.getFullDesc(), pi.getStatus().toLowerCase(Locale.getDefault()), UIUtils.getRelativeTime(pi.getDate())));
        startActivity(Intent.createChooser(share, getString(R.string.share_title)));
        return true;

      case R.id.websro_opt:
        UIUtils.openURL(this, String.format(getString(R.string.websro_url), pi.getCod()));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
    }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    this.getMenuInflater().inflate(R.menu.record_edit_context, menu);

    menu.setHeaderTitle(pi.getSafeDesc());
    menu.findItem(R.id.fav_opt)
      .setTitle(String.format(getString(R.string.opt_toggle_fav), getString(pi.isFav() ? R.string.label_unmark_as : R.string.label_mark_as)));
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case R.id.rename_opt:
        PostalItemDialogFragment.newInstance(R.id.rename_opt, pi).show(getSupportFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      case R.id.fav_opt:
        app.getDatabaseHelper().togglePostalItemFav(pi.getCod());
        pi.toggleFav();
        invalidateOptionsMenu();
        app.setUpdatedList();
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, pi).show(getSupportFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);
    app.setUpdatedList();
    this.pi.setDesc(desc);
    setTitleBar();
  }

  @Override
  public void onDeletePostalItem(PostalItem pi) {
    app.getDatabaseHelper().deletePostalItem(pi.getCod());
    app.setUpdatedList();
    UIUtils.showToast(this, String.format(getString(R.string.toast_item_deleted), pi.getSafeDesc()));
    UIUtils.goHome(this);
  }

  private void setTitleBar() {
    if (pi.getDesc() != null) {
      actionBar.setTitle(pi.getDesc());
      actionBar.setSubtitle(pi.getCod());
    } else {
      actionBar.setTitle(pi.getCod());
    }
  }

  private void handleIntent() {
    Bundle extras = getIntent().getExtras();
        if (extras != null) {
          pi = (PostalItem) extras.getSerializable("postalItem");
        } else {
          finish();
        }

        if (recordFragment == null) {
          recordFragment = PostalRecordFragment.newInstance(pi);
        getSupportFragmentManager().beginTransaction().replace(R.id.record_list, recordFragment).commit();
    } else {
      recordFragment.setPostalItem(pi);
      recordFragment.refreshList();
    }

        setTitleBar();

        if (extras.getBoolean("isNew")) {
          UIUtils.showToast(this, String.format(getString(R.string.toast_item_added), pi.getSafeDesc()));
        }
  }

  private void updateRefreshStatus() {
    if (CarteiroApplication.state.syncing) {
      recordFragment.setRefreshing();
    } else {
      recordFragment.onRefreshComplete();
    }
  }
}
