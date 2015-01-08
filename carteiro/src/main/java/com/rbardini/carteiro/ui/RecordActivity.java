package com.rbardini.carteiro.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class RecordActivity extends ActionBarActivity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener, PostalRecordFragment.OnPostalRecordsChangedListener, WebSROFragment.OnStateChangeListener {
  private CarteiroApplication app;
  private FragmentManager mFragManager;

  private PostalItem pi;
  private boolean onlyWebSRO;
  private PostalRecordFragment recordFragment;
  private WebSROFragment webSROFragment;

  private EditText mTitle;
  private TextView mSubtitle;
  private TextView mLegend;
  private ProgressBar mProgressBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

    supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

    setContentView(R.layout.record);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    Resources res = getResources();
    if (res.getBoolean(R.bool.translucent_status)) {
      UIUtils.addStatusBarPadding(this, R.id.root_layout);
    }

    app = (CarteiroApplication) getApplication();

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);

    mFragManager = getFragmentManager();
    mFragManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        invalidateOptionsMenu();
      }
    });

    mTitle = (EditText) findViewById(R.id.title);
    mSubtitle = (TextView) findViewById(R.id.subtitle);
    mLegend = (TextView) findViewById(R.id.legend);
    mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

    // Hack to enable soft-wrapping on multiple lines in a single line text field
    // http://stackoverflow.com/a/13563946
    mTitle.setHorizontallyScrolling(false);
    mTitle.setMaxLines(3);

    mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          String oldDesc = pi.getSafeDesc();
          String newDesc = mTitle.getText().toString().trim();

          if (newDesc.equals("")) newDesc = null;
          onRenamePostalItem(newDesc, pi);

          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(mTitle.getWindowToken(), 0);

          String toast;
          if (newDesc == null) toast = getString(R.string.toast_item_renamed_empty, pi.getCod());
          else toast = getString(R.string.toast_item_renamed, oldDesc, newDesc);
          UIUtils.showToast(RecordActivity.this, toast);

          return true;
        }

        return false;
      }
    });

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

      menu.findItem(R.id.fav_opt)
        .setIcon(pi.isFav() ? R.drawable.ic_menu_star_on : R.drawable.ic_menu_star_off)
        .setTitle(pi.isFav() ? R.string.opt_unmark_as_fav : R.string.opt_mark_as_fav);

      menu.findItem(R.id.archive_opt)
        .setIcon(pi.isArchived() ? R.drawable.ic_menu_unarchive : R.drawable.ic_menu_archive)
        .setTitle(getString(pi.isArchived() ? R.string.opt_unarchive_item : R.string.opt_archive_item, getString(R.string.category_all)));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final ArrayList<PostalItem> piList = new ArrayList<PostalItem>();
    piList.add(pi);

    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

      case R.id.fav_opt:
        pi.toggleFav();

        app.getDatabaseHelper().togglePostalItemFav(pi.getCod());
        app.setUpdatedList();

        invalidateOptionsMenu();
        return true;

      case R.id.share_opt:
        UIUtils.shareItem(this, pi);
        return true;

      case R.id.place_opt:
        try {
          UIUtils.locateItem(this, pi);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
        return true;

      case R.id.archive_opt:
        pi.toggleArchived();

        app.getDatabaseHelper().togglePostalItemArchived(pi.getCod());
        app.setUpdatedList();

        UIUtils.showToast(this, pi.isArchived() ? getString(R.string.toast_item_archived, pi.getSafeDesc())
            : getString(R.string.toast_item_unarchived, pi.getSafeDesc(), getString(R.string.category_all)));
        invalidateOptionsMenu();
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, piList).show(mFragManager, PostalItemDialogFragment.TAG);
        return true;

      case R.id.websro_opt:
        if (webSROFragment == null) webSROFragment = WebSROFragment.newInstance(pi.getCod());

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
  public void onBackPressed() {
    if (webSROFragment != null && webSROFragment.isVisible() && webSROFragment.canGoBack()) {
      webSROFragment.goBack();
      return;
    }

    if (mFragManager.getBackStackEntryCount() > 0) {
      mFragManager.popBackStack();
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
  public void onDeletePostalItems(ArrayList<PostalItem> piList) {
    final PostalItem pi = piList.get(0);

    app.getDatabaseHelper().deletePostalItem(pi.getCod());
    app.setUpdatedList();
    UIUtils.showToast(this, String.format(getString(R.string.toast_item_deleted), pi.getSafeDesc()));
    UIUtils.goHome(this);
  }

  @Override
  public void onPostalRecordsChanged(List<PostalRecord> postalRecords) {
    setTitleBar();
  }

  @Override
  public void onProgress(int progress) {
    mProgressBar.setVisibility(progress != 100 ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onLeave() {
    mProgressBar.setVisibility(View.GONE);
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
  }

  private void setTitleBar() {
    mTitle.setText(pi.getDesc());
    mLegend.setText(pi.getCod());

    List<PostalRecord> postalRecords = recordFragment.getList();
    PostalRecord pr = postalRecords.get(postalRecords.size() - 1);
    CharSequence relativeDays = "";

    if (!pr.getStatus().equals(PostalUtils.Status.NAO_ENCONTRADO)) {
      relativeDays = UIUtils.getRelativeDaysString(this, pr.getDate());
    }

    mSubtitle.setText(getString(R.string.subtitle_record, relativeDays, pi.getService()));
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
  }

  private void updateRefreshStatus() {
    if (recordFragment != null) {
      if (CarteiroApplication.state.syncing) recordFragment.setRefreshing();
      else recordFragment.onRefreshComplete();
    }
  }
}
