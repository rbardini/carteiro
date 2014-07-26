package com.rbardini.carteiro.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.UIUtils;

import java.util.Locale;

public class RecordActivity extends Activity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  private CarteiroApplication app;
  private FragmentManager mFragManager;

  private PostalItem pi;
  private boolean onlyWebSRO;
  private PostalRecordFragment recordFragment;
  private WebSROFragment webSROFragment;

  private TextView mTitle;
  private TextView mSubtitle;
  private TextView mLegend;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

    setContentView(R.layout.record);
    setProgressBarIndeterminateVisibility(false);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    registerForContextMenu(findViewById(R.id.hidden_edit_opt));

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) UIUtils.addStatusBarPadding(this, R.id.header, false);

    app = (CarteiroApplication) getApplication();

    ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setBackgroundDrawable(new ColorDrawable(Color.argb(0, 0, 0, 0)));

    mFragManager = getFragmentManager();
    mFragManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        invalidateOptionsMenu();
      }
    });

    mTitle = (TextView) findViewById(R.id.title);
    mSubtitle = (TextView) findViewById(R.id.subtitle);
    mLegend = (TextView) findViewById(R.id.legend);

    if (savedInstanceState != null) {
      pi = (PostalItem) savedInstanceState.getSerializable("postalItem");
      onlyWebSRO = savedInstanceState.getBoolean("onlyWebSRO");
    } else {
      handleNewIntent();
    }

    initialize(savedInstanceState == null);
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
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable("postalItem", pi);
    savedInstanceState.putSerializable("onlyWebSRO", onlyWebSRO);

    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleNewIntent();
    initialize(false);
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
    if (getCurrentFragment() instanceof PostalRecordFragment) {
      getMenuInflater().inflate(R.menu.record_actions, menu);

      if (pi.isFav()) {
        menu.findItem(R.id.fav_opt).setIcon(R.drawable.ic_action_star);
      }
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.fav_opt:
        app.getDatabaseHelper().togglePostalItemFav(pi.getCod());
        item.setIcon(pi.toggleFav() ? R.drawable.ic_action_star : R.drawable.ic_action_star_off);
        app.setUpdatedList();
        return true;

      case R.id.place_opt:
        try {
          UIUtils.locateItem(this, pi);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
        return true;

      case R.id.edit_opt:
        openContextMenu(findViewById(R.id.hidden_edit_opt));
        return true;

      case R.id.share_opt:
        UIUtils.shareItem(this, pi);
        return true;

      case R.id.websro_opt:
        if (webSROFragment == null) {
          webSROFragment = WebSROFragment.newInstance(pi.getCod());
        }

        mFragManager
          .beginTransaction()
          .replace(R.id.record_list, webSROFragment, WebSROFragment.TAG)
          .addToBackStack(null)
          .commit();

        return true;

      default:
        return super.onOptionsItemSelected(item);
      }
    }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    getMenuInflater().inflate(R.menu.record_edit_context, menu);

    menu.setHeaderTitle(pi.getSafeDesc());
    menu.findItem(R.id.archive_opt).setTitle(pi.isArchived() ? getString(R.string.opt_unarchive_item, getString(R.string.category_all))
                                                             : getString(R.string.opt_archive_item));
  }

  @Override
  public boolean onContextItemSelected(android.view.MenuItem item) {
    switch (item.getItemId()) {
      case R.id.rename_opt:
        PostalItemDialogFragment.newInstance(R.id.rename_opt, pi).show(mFragManager, PostalItemDialogFragment.TAG);
        return true;

      case R.id.archive_opt:
        app.getDatabaseHelper().togglePostalItemArchived(pi.getCod());
        app.setUpdatedList();
        UIUtils.showToast(this, pi.toggleArchived() ? getString(R.string.toast_item_archived, pi.getSafeDesc())
                                                    : getString(R.string.toast_item_unarchived, pi.getSafeDesc(), getString(R.string.category_all)));
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, pi).show(mFragManager, PostalItemDialogFragment.TAG);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (webSROFragment != null && webSROFragment.isVisible() && webSROFragment.canGoBack()) {
      webSROFragment.goBack();
      return;
    }

    super.onBackPressed();
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
    boolean hasDesc = pi.getDesc() != null;

    if (hasDesc) {
      mTitle.setText(pi.getDesc());
      mLegend.setText(pi.getCod());
    } else {
      mTitle.setText(pi.getCod());
    }
    mLegend.setVisibility(hasDesc ? View.VISIBLE : View.GONE);

    mSubtitle.setText(getString(R.string.subtitle_record, pi.getService()).toUpperCase(Locale.getDefault()));
    mSubtitle.setCompoundDrawablesWithIntrinsicBounds(pi.getFlag(this), 0, 0, 0);
  }

  private Fragment getCurrentFragment() {
    return mFragManager.findFragmentById(R.id.record_list);
  }

  private void handleNewIntent() {
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    if (extras != null) {
      pi = (PostalItem) extras.getSerializable("postalItem");
    } else {
      finish();
    }

    if (extras.getBoolean("isNew")) {
      UIUtils.showToast(this, String.format(getString(R.string.toast_item_added), pi.getSafeDesc()));
      intent.removeExtra("isNew");
    }

    String action = intent.getAction();
    if (action != null) {
      if (action.equals("locate")) {
        try {
          UIUtils.locateItem(this, pi);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
      } else if (action.equals("share")) {
        UIUtils.shareItem(this, pi);
      } else if (action.equals("webSRO")) {
        onlyWebSRO = true;
      }

      NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      nm.cancel(R.string.app_name);
    }

    intent.removeExtra("postalItem");
  }

  private void initialize(boolean isNewInstance) {
    if (onlyWebSRO) {
      webSROFragment = WebSROFragment.newInstance(pi.getCod());
      mFragManager.beginTransaction().replace(R.id.record_list, webSROFragment, WebSROFragment.TAG).commit();
    } else if (isNewInstance) {
      recordFragment = PostalRecordFragment.newInstance(pi);
      mFragManager.beginTransaction().replace(R.id.record_list, recordFragment, PostalRecordFragment.TAG).commit();
    } else {
      recordFragment = (PostalRecordFragment) mFragManager.findFragmentByTag(PostalRecordFragment.TAG);
      recordFragment.setPostalItem(pi);
      recordFragment.refreshList();
    }

    setTitleBar();
  }

  private void updateRefreshStatus() {
    if (recordFragment != null) {
      if (CarteiroApplication.state.syncing) recordFragment.setRefreshing();
      else recordFragment.onRefreshComplete();
    }
  }
}
