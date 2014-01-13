package com.rbardini.carteiro.ui;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.SearchView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.ui.MainPagerAdapter.OnPostPageChangeListener;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  protected static final String TAG = "MainActivity";
  public static final int ADD_REQUEST = 1;

  private CarteiroApplication app;
  private FragmentManager fragManager;
  private MainPagerAdapter pagerAdapter;
  private ShareActionProvider mShareActionProvider;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    app = (CarteiroApplication) getApplication();
    fragManager = getSupportFragmentManager();
    pagerAdapter = new MainPagerAdapter(this, fragManager);
    pagerAdapter.setOnPostPageChangeListener(new OnPostPageChangeListener() {
      @Override
      public void onPostPageSelected(int position) { setShareIntent(); }
    });

    ViewPager viewPager = (ViewPager) findViewById(R.id.postal_list_pager);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(3);
    viewPager.setCurrentItem(3);

    TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
    indicator.setViewPager(viewPager);
    indicator.setOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) { updateRefreshStatus(); }

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageScrollStateChanged(int state) {}
    });
  }

  @Override
  protected void onResume() {
    super.onResume();

    CarteiroApplication.state.receiver.setReceiver(this);
    updateRefreshStatus();
    if (app.hasUpdate()) {
      refreshList();
    }
    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(SyncService.NOTIFICATION_NEW_UPDATE);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (!CarteiroApplication.state.syncing) {
          app.clearUpdate();
        }
    CarteiroApplication.state.receiver.clearReceiver();
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
        if (app.hasUpdate()) {
          refreshList();
        }
        break;
      }
      case SyncService.STATUS_ERROR: {
        updateRefreshStatus();
        final String error = getString(R.string.toast_sync_error, resultData.getString(Intent.EXTRA_TEXT));
        UIUtils.showToast(this, error);
        break;
      }
    }
  }

  @Override @TargetApi(11)
  public boolean onCreateOptionsMenu(Menu menu) {
    getSupportMenuInflater().inflate(R.menu.main_actions, menu);

    mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.share_opt).getActionProvider();
    mShareActionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
    setShareIntent();

    MenuItem searchViewButton = menu.findItem(R.id.search_view_opt);
    if (searchViewButton != null) {
      SearchView searchView = (SearchView) searchViewButton.getActionView();
      SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    try {
      int listSize = pagerAdapter.getCurrentView().getList().size();
      menu.findItem(R.id.share_opt).setEnabled(listSize > 0);
    } catch (Exception e) {}

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.add_opt:
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
        return true;

      case R.id.search_opt:
        onSearchRequested();
        return true;

      // Workaround for ActionBarSherlock issue #724
      case R.id.share_opt:
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
          Intent shareIntent = buildShareIntent();
          if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
          }
        }
        return true;

      case R.id.preferences_opt:
        startActivity(new Intent(this, PreferencesActivity.class));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);
    UIUtils.showToast(this, String.format(getString(R.string.toast_item_renamed), pi.getSafeDesc(), desc));
    refreshList();
  }

  @Override
  public void onDeletePostalItem(PostalItem pi) {
    app.getDatabaseHelper().deletePostalItem(pi.getCod());
    UIUtils.showToast(this, String.format(getString(R.string.toast_item_deleted), pi.getSafeDesc()));
    refreshList();
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    refreshList();
    }

  private void setShareIntent() {
    if (mShareActionProvider != null) {
      mShareActionProvider.setShareIntent(buildShareIntent());
    }
  }

  private Intent buildShareIntent() {
    return PostalUtils.getShareIntent(this, pagerAdapter.getCurrentView().getList());
  }

  private void updateRefreshStatus() {
    if (CarteiroApplication.state.syncing) {
      pagerAdapter.setRefreshing();
    } else {
      pagerAdapter.onRefreshComplete();
    }
  }

  public void refreshList() {
    pagerAdapter.notifyDataSetChanged();
    setShareIntent();
  }
}
