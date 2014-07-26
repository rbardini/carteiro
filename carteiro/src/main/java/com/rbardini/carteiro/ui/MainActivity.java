package com.rbardini.carteiro.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class MainActivity extends Activity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  protected static final String TAG = "MainActivity";

  private CarteiroApplication app;
  private ActionBar mActionBar;
  private FragmentManager mFragmentManager;
  private NotificationManager mNotificationManager;
  private DrawerLayout mDrawerLayout;
  private StickyListHeadersListView mDrawerList;
  private int[] mDrawerItems;
  private ActionBarDrawerToggle mDrawerToggle;
  private CharSequence mTitle;
  private PostalListFragment mCurrentFragment;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) UIUtils.addStatusBarPadding(this, R.id.root_layout, true);

    app = (CarteiroApplication) getApplication();
    mActionBar = getActionBar();
    mFragmentManager = getFragmentManager();
    mFragmentManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        mCurrentFragment = getCurrentFragment();
      }
    });
    mNotificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));

    mActionBar.setDisplayHomeAsUpEnabled(true);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (StickyListHeadersListView) findViewById(R.id.nav_drawer);
    mDrawerList.setAreHeadersSticky(false);
    mDrawerItems = new int[] {
      Category.ALL,       Category.FAVORITES, Category.AVAILABLE,
      Category.DELIVERED, Category.IRREGULAR, Category.UNKNOWN,
      Category.RETURNED,  Category.ARCHIVED
    };
    mDrawerList.setAdapter(new DrawerListAdapter(this, mDrawerItems));
    mDrawerList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showCategory(position);
      }
    });
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
      @Override
      public void onDrawerClosed(View drawerView) {
        mActionBar.setTitle(mTitle);
        invalidateOptionsMenu();
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        mActionBar.setTitle(R.string.app_name);
        invalidateOptionsMenu();
      }
    };
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    if (savedInstanceState == null) showCategory(0);
    else mCurrentFragment = getCurrentFragment();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
    if (mDrawerLayout.isDrawerOpen(mDrawerList)) mActionBar.setTitle(R.string.app_name);
  }

  @Override
  protected void onResume() {
    super.onResume();

    CarteiroApplication.state.receiver.setReceiver(this);
    updateRefreshStatus();
    if (app.hasUpdate()) refreshList();

    mNotificationManager.cancel(SyncService.NOTIFICATION_NEW_UPDATE);
  }

  @Override
  protected void onPause() {
    super.onPause();

    if (!CarteiroApplication.state.syncing) app.clearUpdate();
    CarteiroApplication.state.receiver.clearReceiver();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
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
        if (app.hasUpdate()) refreshList();
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

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_actions, menu);

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
    boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

    menu.findItem(R.id.add_opt).setVisible(!drawerOpen);

    MenuItem searchItem = menu.findItem(R.id.search_view_opt);
    searchItem.setVisible(!drawerOpen);

    MenuItem shareItem = menu.findItem(R.id.share_opt);
    shareItem.setVisible(!drawerOpen);
    try {
      int listSize = mCurrentFragment.getListSize();
      shareItem.setEnabled(listSize > 0);
    } catch (Exception e) {}

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
          mDrawerLayout.closeDrawer(mDrawerList);
        } else {
          mDrawerLayout.openDrawer(mDrawerList);
        }
        return true;

      case R.id.add_opt:
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
        return true;

      case R.id.share_opt:
        Intent shareIntent = PostalUtils.getShareIntent(this, mCurrentFragment.getList());
        if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
        return true;

      case R.id.preferences_opt:
        startActivity(new Intent(this, PreferencesActivity.class));
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void setTitle(CharSequence title) {
    mTitle = title;
    mActionBar.setTitle(mTitle);
  }

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);

    String toast;
    if (desc == null) toast = getString(R.string.toast_item_renamed_empty, pi.getCod());
    else toast = getString(R.string.toast_item_renamed, pi.getSafeDesc(), desc);

    UIUtils.showToast(this, toast);
    refreshList();
  }

  @Override
  public void onDeletePostalItem(PostalItem pi) {
    app.getDatabaseHelper().deletePostalItem(pi.getCod());
    UIUtils.showToast(this, getString(R.string.toast_item_deleted, pi.getSafeDesc()));
    refreshList();
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    refreshList();
  }

  public void setDrawerCategoryChecked(int category) {
    for (int i=0; i<mDrawerItems.length; i++) {
      if (mDrawerItems[i] == category) {
        mDrawerList.setItemChecked(i, true);
        break;
      }
    }
  }

  private void showCategory(int position) {
    int category = mDrawerItems[position];

    // Only replace fragment if it is a different category
    if (mCurrentFragment == null || mCurrentFragment.getCategory() != category) {
      PostalListFragment newFragment = PostalListFragment.newInstance(category);
      String name = getString(Category.getTitle(category));

      mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      FragmentTransaction ft = mFragmentManager
          .beginTransaction()
          .replace(R.id.main_container, newFragment, name);
      if (mCurrentFragment != null && category != Category.ALL) ft.addToBackStack(name);  // Avoid adding empty main container and duplicate "all" category to the back stack
      ft.commit();

      mCurrentFragment = newFragment;
    }

    mDrawerLayout.closeDrawer(mDrawerList);
  }

  private PostalListFragment getCurrentFragment() {
    return (PostalListFragment) mFragmentManager.findFragmentById(R.id.main_container);
  }

  private void updateRefreshStatus() {
    if (CarteiroApplication.state.syncing) mCurrentFragment.setRefreshing();
    else mCurrentFragment.onRefreshComplete();
  }

  public void refreshList() {
    mCurrentFragment.refreshList(false);
  }
}
