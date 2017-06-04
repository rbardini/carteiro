package com.rbardini.carteiro.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.ui.transition.RoundIconTransition;
import com.rbardini.carteiro.ui.transition.MorphTransition;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.Collections;

public class RecordActivity extends PostalActivity implements SROFragment.OnStateChangeListener {
  protected static final String TAG = "RecordActivity";

  private DatabaseHelper dh;
  private FragmentManager mFragManager;

  private PostalItem mPostalItem;
  private boolean mOnlySRO;
  private PostalRecordFragment mRecordFragment;
  private SROFragment mSROFragment;

  private TextView mSubtitle;
  private TextView mLegend;
  private ProgressBar mProgressBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    dh = app.getDatabaseHelper();
    mFragManager = getFragmentManager();
    mFragManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        invalidateOptionsMenu();
      }
    });

    mSubtitle = (TextView) findViewById(R.id.subtitle);
    mLegend = (TextView) findViewById(R.id.legend);
    mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

    if (savedInstanceState != null) {
      mPostalItem = (PostalItem) savedInstanceState.getSerializable("postalItem");
      mOnlySRO = savedInstanceState.getBoolean("onlySRO");
    } else {
      handleNewIntent();
    }

    setupTransition();
    setTitleBar();
    setFragment(savedInstanceState == null);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable("postalItem", mPostalItem);
    savedInstanceState.putSerializable("onlySRO", mOnlySRO);

    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleNewIntent();
    setFragment(false);
  }

  @Override
  public void onSyncStatusChange(Intent intent) {
    int status = intent.getIntExtra(SyncService.EXTRA_STATUS, SyncService.STATUS_FINISHED);

    switch (status) {
      case SyncService.STATUS_FINISHED:
        updateRefreshStatus();
        if (app.hasUpdate() && app.isUpdatedCod(mPostalItem.getCod())) mRecordFragment.refreshList();
        break;

      default:
        super.onSyncStatusChange(intent);
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
    final ArrayList<PostalItem> piList = new ArrayList<>();
    piList.add(mPostalItem);

    switch (item.getItemId()) {
      case android.R.id.home:
        finishAfterTransition();
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

      case R.id.rename_opt:
        PostalItemDialogFragment.newInstance(R.id.rename_opt,
            new ArrayList<>(Collections.singletonList(mPostalItem))).show(getFragmentManager(), PostalItemDialogFragment.TAG);
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

      case R.id.sro_opt:
        if (mSROFragment == null) mSROFragment = SROFragment.newInstance(mPostalItem.getCod());
        mFragManager.beginTransaction().replace(R.id.content, mSROFragment, SROFragment.TAG).addToBackStack(null).commit();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onBackPressed() {
    if (mSROFragment != null && mSROFragment.isVisible() && mSROFragment.canGoBack()) {
      mSROFragment.goBack();
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
    super.onRenamePostalItem(desc, pi);

    mPostalItem.setDesc(desc);
    setActionBarTitle(mPostalItem.getSafeDesc());
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
  public PostalFragment getPostalFragment() {
    return mRecordFragment;
  }

  private void setupTransition() {
    View content = findViewById(R.id.app_bar);

    if (!RoundIconTransition.setup(this, content)) {
      int appBarColor = ContextCompat.getColor(this, R.color.theme_accent);
      int appBarRadius = 0;

      MorphTransition.setup(this, content, appBarColor, appBarRadius);
    }
  }

  private void setTitleBar() {
    mLegend.setText(mPostalItem.getCod());
    setActionBarTitle(mPostalItem.getSafeDesc());

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

    mLegend.setCompoundDrawablesWithIntrinsicBounds(0, 0, mPostalItem.getFlag(this), 0);
  }

  private void setActionBarTitle(CharSequence title) {
    CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    collapsingToolbar.setTitle(title);
  }

  private Fragment getCurrentFragment() {
    return mFragManager.findFragmentById(R.id.content);
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

        case "sro":
          mOnlySRO = true;
          break;
      }

      NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      nm.cancel(R.string.app_name);
    }

    intent.removeExtra("postalItem");
  }

  private void setFragment(boolean isNewInstance) {
    if (mOnlySRO) {
      mSROFragment = SROFragment.newInstance(mPostalItem.getCod());
      mFragManager.beginTransaction().replace(R.id.content, mSROFragment, SROFragment.TAG).commit();

    } else if (isNewInstance) {
      mRecordFragment = PostalRecordFragment.newInstance(mPostalItem);
      mFragManager.beginTransaction().replace(R.id.content, mRecordFragment, PostalRecordFragment.TAG).commit();

    } else {
      mRecordFragment = (PostalRecordFragment) mFragManager.findFragmentByTag(PostalRecordFragment.TAG);
      mRecordFragment.setPostalItem(mPostalItem);
      mRecordFragment.refreshList();
    }
  }
}
