package com.rbardini.carteiro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.svc.SyncTask;
import com.rbardini.carteiro.util.AnalyticsUtils;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener;

public class RecordActivity extends ShipmentActivity implements SROFragment.OnStateChangeListener {
  protected static final String TAG = "RecordActivity";

  private static final String SHIPMENT_KEY = "shipment";
  private static final String ONLY_SRO_KEY = "onlySRO";

  public static final String EXTRA_IS_NEW_SHIPMENT = TAG + ".EXTRA_IS_NEW_SHIPMENT";
  public static final String EXTRA_SHIPMENT = TAG + ".EXTRA_SHIPMENT";
  public static final String ACTION_ARCHIVE = TAG + ".ACTION_ARCHIVE";
  public static final String ACTION_DELETE = TAG + ".ACTION_DELETE";
  public static final String ACTION_LOCATE = TAG + ".ACTION_LOCATE";
  public static final String ACTION_SHARE = TAG + ".ACTION_SHARE";
  public static final String ACTION_SRO = TAG + ".ACTION_SRO";

  private DatabaseHelper dh;
  private FragmentManager mFragManager;

  private Shipment mShipment;
  private boolean mOnlySRO;
  private RecordFragment mRecordFragment;
  private SROFragment mSROFragment;

  private TextView mSubtitle;
  private TextView mLegend;
  private ProgressBar mProgressBar;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);
    supportPostponeEnterTransition();

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    dh = app.getDatabaseHelper();
    mFragManager = getSupportFragmentManager();
    mFragManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        invalidateOptionsMenu();
      }
    });

    mSubtitle = findViewById(R.id.subtitle);
    mLegend = findViewById(R.id.legend);
    mProgressBar = findViewById(R.id.progress_bar);

    if (savedInstanceState != null) {
      mShipment = (Shipment) savedInstanceState.getSerializable(SHIPMENT_KEY);
      mOnlySRO = savedInstanceState.getBoolean(ONLY_SRO_KEY);
    } else {
      handleNewIntent();
    }

    setTitleBar();
    setFragment(savedInstanceState == null);
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putSerializable(SHIPMENT_KEY, mShipment);
    savedInstanceState.putSerializable(ONLY_SRO_KEY, mOnlySRO);

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
    int status = intent.getIntExtra(SyncTask.EXTRA_STATUS, SyncTask.STATUS_FINISHED);

    switch (status) {
      case SyncTask.STATUS_FINISHED:
        updateRefreshStatus();
        mRecordFragment.refreshList();
        break;

      default:
        super.onSyncStatusChange(intent);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (getCurrentFragment() instanceof RecordFragment) {
      getMenuInflater().inflate(R.menu.record_actions, menu);

      menu.findItem(R.id.fav_opt)
        .setIcon(mShipment.isFavorite() ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp)
        .setTitle(mShipment.isFavorite() ? R.string.opt_unmark_as_fav : R.string.opt_mark_as_fav);

      menu.findItem(R.id.archive_opt)
        .setIcon(mShipment.isArchived() ? R.drawable.ic_unarchive_white_24dp : R.drawable.ic_archive_white_24dp)
        .setTitle(getString(mShipment.isArchived() ? R.string.opt_unarchive_item : R.string.opt_archive_item, getString(R.string.category_all)));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;

      case R.id.fav_opt:
        mShipment.toggleFavorite();
        dh.togglePostalItemFav(mShipment.getNumber());

        invalidateOptionsMenu();
        return true;

      case R.id.share_opt:
        UIUtils.shareItem(this, mShipment);
        return true;

      case R.id.place_opt:
        try {
          UIUtils.locateItem(this, mShipment);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
        return true;

      case R.id.rename_opt:
        ShipmentRenameDialogFragment.newInstance(mShipment).show(getSupportFragmentManager(), ShipmentRenameDialogFragment.TAG);
        return true;

      case R.id.archive_opt:
        if (mShipment.isArchived()) {
          mShipment.toggleArchived();
          dh.toggleShipmentArchived(mShipment);

          UIUtils.showToast(this, R.string.toast_unarchived_count_single);
          invalidateOptionsMenu();
        } else {
          final Intent intent = new Intent(this, MainActivity.class)
            .putExtra(EXTRA_SHIPMENT, mShipment)
            .setAction(ACTION_ARCHIVE)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

          startActivity(intent);
          finish();
        }

        return true;

      case R.id.delete_opt:
        final Intent intent = new Intent(this, MainActivity.class)
          .putExtra(EXTRA_SHIPMENT, mShipment)
          .setAction(ACTION_DELETE)
          .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
        return true;

      case R.id.sro_opt:
        if (mSROFragment == null) mSROFragment = SROFragment.newInstance(mShipment.getNumber());
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
  public void onRenameShipment(String desc, Shipment shipment) {
    super.onRenameShipment(desc, shipment);

    mShipment.setName(desc);
    setActionBarTitle(mShipment.getDescription());
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
  public ShipmentFragment getPostalFragment() {
    return mRecordFragment;
  }

  private void setTitleBar() {
    mLegend.setText(mShipment.getNumber());
    setActionBarTitle(mShipment.getDescription());

    CharSequence relativeDays = "";
    try {
      ShipmentRecord record = dh.getFirstShipmentRecord(mShipment.getNumber());
      if (!record.getStatus().equals(PostalUtils.Status.NAO_ENCONTRADO)) {
        relativeDays = UIUtils.getRelativeDaysString(this, record.getDate());
      }

    } catch (Exception e) {
      Log.w(TAG, "Could not get first postal activity_record for postal item " + mShipment.getNumber(), e);

    } finally {
      mSubtitle.setText(getString(R.string.subtitle_record, relativeDays, mShipment.getService()).trim());
      // TODO Set title bar again when postal records updated
    }

    mLegend.setCompoundDrawablesWithIntrinsicBounds(0, 0, mShipment.getFlag(this), 0);
  }

  private void setActionBarTitle(CharSequence title) {
    CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
    collapsingToolbar.setTitle(title);
  }

  private Fragment getCurrentFragment() {
    return mFragManager.findFragmentById(R.id.content);
  }

  private void handleNewIntent() {
    Intent intent = getIntent();
    Bundle extras = intent.getExtras();

    if (extras == null) {
      finishAfterTransition();
      return;
    }

    mShipment = (Shipment) extras.getSerializable(EXTRA_SHIPMENT);
    intent.removeExtra(EXTRA_SHIPMENT);

    if (mShipment == null) {
      finishAfterTransition();
      return;
    }

    if (mShipment.isUnread()) {
      dh.readPostalItem(mShipment.getNumber());
      mShipment.setUnread(false);
    }

    if (extras.getBoolean(EXTRA_IS_NEW_SHIPMENT)) {
      String message = String.format(getString(R.string.toast_item_added), mShipment.getDescription());
      Snackbar
        .make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
        .setAction(R.string.add_another_btn, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(RecordActivity.this, AddActivity.class);
            startActivity(intent);
            finishAfterTransition();
          }
        })
        .show();
      intent.removeExtra(EXTRA_IS_NEW_SHIPMENT);
    }

    AnalyticsUtils.recordShipmentView(this, mShipment);

    String action = intent.getAction();
    if (action != null) {
      handleAction(action);
    }
  }

  private void handleAction(String action) {
    switch (action) {
      case ACTION_LOCATE:
        try {
          UIUtils.locateItem(this, mShipment);
        } catch (Exception e) {
          UIUtils.showToast(this, e.getMessage());
        }
        break;

      case ACTION_SHARE:
        UIUtils.shareItem(this, mShipment);
        break;

      case ACTION_SRO:
        mOnlySRO = true;
        break;
    }
  }

  private void setFragment(boolean isNewInstance) {
    if (mOnlySRO) {
      mSROFragment = SROFragment.newInstance(mShipment.getNumber());
      mFragManager.beginTransaction().replace(R.id.content, mSROFragment, SROFragment.TAG).commit();
      return;
    }

    if (isNewInstance) {
      mRecordFragment = RecordFragment.newInstance(mShipment);
      mFragManager.beginTransaction().replace(R.id.content, mRecordFragment, RecordFragment.TAG).commit();
      return;
    }

    mRecordFragment = (RecordFragment) mFragManager.findFragmentByTag(RecordFragment.TAG);
    mRecordFragment.setShipment(mShipment);
    mRecordFragment.refreshList();
  }
}
