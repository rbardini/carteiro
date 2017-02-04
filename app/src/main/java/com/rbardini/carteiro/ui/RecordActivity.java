package com.rbardini.carteiro.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public class RecordActivity extends PostalActivity implements WebSROFragment.OnStateChangeListener {
  protected static final String TAG = "RecordActivity";

  private DatabaseHelper dh;
  private FragmentManager mFragManager;

  private PostalItem mPostalItem;
  private boolean mOnlyWebSRO;
  private PostalRecordFragment mRecordFragment;
  private WebSROFragment mWebSROFragment;

  private EditText mTitle;
  private TextView mSubtitle;
  private TextView mLegend;
  private ProgressBar mProgressBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    supportRequestWindowFeature(AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY);
    setContentView(R.layout.activity_record);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
    actionBar.setDisplayShowTitleEnabled(false);

    dh = app.getDatabaseHelper();
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
          String oldDesc = mPostalItem.getSafeDesc();
          String newDesc = mTitle.getText().toString().trim();

          if (newDesc.equals("")) newDesc = null;
          onRenamePostalItem(newDesc, mPostalItem);

          InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(mTitle.getWindowToken(), 0);

          String toast;
          if (newDesc == null) toast = getString(R.string.toast_item_renamed_empty, mPostalItem.getCod());
          else toast = getString(R.string.toast_item_renamed, oldDesc, newDesc);
          UIUtils.showToast(RecordActivity.this, toast);

          return true;
        }

        return false;
      }
    });

    if (savedInstanceState != null) {
      mPostalItem = (PostalItem) savedInstanceState.getSerializable("postalItem");
      mOnlyWebSRO = savedInstanceState.getBoolean("onlyWebSRO");
    } else {
      handleNewIntent();
    }

    initialize(savedInstanceState == null);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable("postalItem", mPostalItem);
    savedInstanceState.putSerializable("onlyWebSRO", mOnlyWebSRO);

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
      case SyncService.STATUS_FINISHED:
        updateRefreshStatus();
        if (app.hasUpdate() && app.isUpdatedCod(mPostalItem.getCod())) mRecordFragment.refreshList();
        break;

      default:
        super.onReceiveResult(resultCode, resultData);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (getCurrentFragment() instanceof PostalRecordFragment) {
      getMenuInflater().inflate(R.menu.record_actions, menu);

      menu.findItem(R.id.fav_opt)
        .setIcon(mPostalItem.isFav() ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp)
        .setTitle(mPostalItem.isFav() ? R.string.opt_unmark_as_fav : R.string.opt_mark_as_fav);

      menu.findItem(R.id.archive_opt)
        .setIcon(mPostalItem.isArchived() ? R.drawable.ic_unarchive_white_24dp : R.drawable.ic_archive_white_24dp)
        .setTitle(getString(mPostalItem.isArchived() ? R.string.opt_unarchive_item : R.string.opt_archive_item, getString(R.string.category_all)));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    final ArrayList<PostalItem> piList = new ArrayList<PostalItem>();
    piList.add(mPostalItem);

    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;

      case R.id.fav_opt:
        mPostalItem.toggleFav();

        dh.togglePostalItemFav(mPostalItem.getCod());
        app.setUpdatedList();

        invalidateOptionsMenu();
        return true;

      case R.id.share_opt:
        UIUtils.shareItem(this, mPostalItem);
        return true;

      case R.id.place_opt:
        try {
          UIUtils.locateItem(this, mPostalItem);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
        return true;

      case R.id.archive_opt:
        mPostalItem.toggleArchived();

        dh.togglePostalItemArchived(mPostalItem.getCod());
        app.setUpdatedList();

        UIUtils.showToast(this, mPostalItem.isArchived() ? getString(R.string.toast_item_archived, mPostalItem.getSafeDesc())
            : getString(R.string.toast_item_unarchived, mPostalItem.getSafeDesc(), getString(R.string.category_all)));
        invalidateOptionsMenu();
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, piList).show(mFragManager, PostalItemDialogFragment.TAG);
        return true;

      case R.id.websro_opt:
        if (mWebSROFragment == null) mWebSROFragment = WebSROFragment.newInstance(mPostalItem.getCod());
        mFragManager.beginTransaction().replace(R.id.record_list, mWebSROFragment, WebSROFragment.TAG).addToBackStack(null).commit();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (mWebSROFragment != null && mWebSROFragment.isVisible() && mWebSROFragment.canGoBack()) {
      mWebSROFragment.goBack();
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
    dh.renamePostalItem(pi.getCod(), desc);
    app.setUpdatedList();
    this.mPostalItem.setDesc(desc);
    setTitleBar();
  }

  @Override
  public void onDeletePostalItems(ArrayList<PostalItem> piList) {
    final PostalItemRecord pir = new PostalItemRecord(piList.get(0));

    pir.deleteFrom(dh);
    app.setUpdatedList();

    UIUtils.showToast(this, String.format(getString(R.string.toast_item_deleted), pir.getSafeDesc()));
    UIUtils.goHome(this);
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

  @Override
  public PostalFragment getPostalFragment() {
    return mRecordFragment;
  }

  private void setTitleBar() {
    mTitle.setText(mPostalItem.getDesc());
    mLegend.setText(mPostalItem.getCod());

    CharSequence relativeDays = "";
    try {
      PostalRecord pr = dh.getFirstPostalRecord(mPostalItem.getCod());
      if (!pr.getStatus().equals(PostalUtils.Status.NAO_ENCONTRADO)) {
        relativeDays = UIUtils.getRelativeDaysString(this, pr.getDate());
      }

    } catch (Exception e) {
      Log.w(TAG, "Could not get first postal activity_record for postal item " + mPostalItem.getCod(), e);

    } finally {
      mSubtitle.setText(getString(R.string.subtitle_record, relativeDays, mPostalItem.getService()));
      // TODO Set title bar again when postal records updated
    }

    mSubtitle.setCompoundDrawablesWithIntrinsicBounds(mPostalItem.getFlag(this), 0, 0, 0);
  }

  private Fragment getCurrentFragment() {
    return mFragManager.findFragmentById(R.id.record_list);
  }

  private void handleNewIntent() {
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    if (extras != null) {
      mPostalItem = (PostalItem) extras.getSerializable("postalItem");

      if (extras.getBoolean("isNew")) {
        String message = String.format(getString(R.string.toast_item_added), mPostalItem.getSafeDesc());
        Snackbar
          .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
          .setAction(R.string.add_another_btn, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(RecordActivity.this, AddActivity.class);
              startActivity(intent);
              finish();
            }
          })
          .show();
        intent.removeExtra("isNew");
      }

    } else {
      finish();
    }

    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case "locate":
          try {
            UIUtils.locateItem(this, mPostalItem);
          } catch (Exception e) {
            UIUtils.showToast(this, e.getMessage());
          }
          break;

        case "share":
          UIUtils.shareItem(this, mPostalItem);
          break;

        case "webSRO":
          mOnlyWebSRO = true;
          break;
      }

      NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      nm.cancel(R.string.app_name);
    }

    intent.removeExtra("postalItem");
  }

  private void initialize(boolean isNewInstance) {
    if (mOnlyWebSRO) {
      mWebSROFragment = WebSROFragment.newInstance(mPostalItem.getCod());
      mFragManager.beginTransaction().replace(R.id.record_list, mWebSROFragment, WebSROFragment.TAG).commit();

    } else if (isNewInstance) {
      mRecordFragment = PostalRecordFragment.newInstance(mPostalItem);
      mFragManager.beginTransaction().replace(R.id.record_list, mRecordFragment, PostalRecordFragment.TAG).commit();

    } else {
      mRecordFragment = (PostalRecordFragment) mFragManager.findFragmentByTag(PostalRecordFragment.TAG);
      mRecordFragment.setPostalItem(mPostalItem);
      mRecordFragment.refreshList();
    }

    setTitleBar();
  }
}
