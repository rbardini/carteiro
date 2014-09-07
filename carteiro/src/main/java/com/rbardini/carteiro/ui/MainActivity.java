package com.rbardini.carteiro.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
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
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;

public class MainActivity extends Activity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  protected static final String TAG = "MainActivity";

  // Delay to launch navigation drawer item, to allow close animation to play
  private static final int NAVDRAWER_LAUNCH_DELAY = 250;

  // Fade in and fade out durations for the main content when switching between
  // different fragments through the navigation drawer
  private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
  private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

  private CarteiroApplication app;
  private ActionBar mActionBar;
  private FragmentManager mFragmentManager;
  private NotificationManager mNotificationManager;
  private Handler mHandler;
  private View mMainContainer;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private DrawerListAdapter mDrawerListAdapter;
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
    mHandler = new Handler();

    mActionBar.setDisplayHomeAsUpEnabled(true);
    mMainContainer = findViewById(R.id.main_content);
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerList = (ListView) findViewById(R.id.nav_drawer);
    mDrawerListAdapter = new DrawerListAdapter(this);
    mDrawerList.setAdapter(mDrawerListAdapter);
    mDrawerList.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final DrawerListAdapter.DrawerModel model = mDrawerListAdapter.getItem(position);

        // Fade out main content view if a new fragment will be shown
        if (model.action == DrawerListAdapter.ACTION_CATEGORY && model.id != mCurrentFragment.getCategory()) {
          mMainContainer.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        // Launch item after a short delay, to allow navigation drawer close animation to play
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            switch (model.action) {
              case DrawerListAdapter.ACTION_CATEGORY:
                showCategory(model.id);
                break;

              case DrawerListAdapter.ACTION_SETTINGS:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                break;

              case DrawerListAdapter.ACTION_FEEDBACK:
                UIUtils.openURL(MainActivity.this, getString(R.string.feedback_url));
                break;
            }
          }
        }, NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawer(mDrawerList);
      }
    });
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer_indicator, R.string.drawer_open, R.string.drawer_close) {
      @Override
      public void onDrawerClosed(View drawerView) {
        mActionBar.setTitle(mTitle);
        invalidateOptionsMenu();
      }

      @Override
      public void onDrawerOpened(View drawerView) {
        mCurrentFragment.clearSelection();
        mActionBar.setTitle(R.string.app_name);
        invalidateOptionsMenu();
      }
    };
    mDrawerLayout.setDrawerListener(mDrawerToggle);
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

    if (savedInstanceState == null) showCategory(Category.ALL);
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
    final int itemId = item.getItemId();

    switch (itemId) {
      case android.R.id.home:
        if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
          mDrawerLayout.closeDrawer(mDrawerList);
        } else {
          mDrawerLayout.openDrawer(mDrawerList);
        }
        return true;

      case R.id.add_opt:
        Intent intent = new Intent(this, AddActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          View v = findViewById(itemId);
          ActivityOptions scaleAnim = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
          startActivity(intent, scaleAnim.toBundle());

        } else {
          startActivity(intent);
        }
        return true;

      case R.id.share_opt:
        Intent shareIntent = PostalUtils.getShareIntent(this, mCurrentFragment.getList());
        if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
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
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
      mDrawerLayout.closeDrawer(mDrawerList);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onRenamePostalItem(String desc, PostalItem pi) {
    app.getDatabaseHelper().renamePostalItem(pi.getCod(), desc);

    String toast;
    if (desc == null) toast = getString(R.string.toast_item_renamed_empty, pi.getCod());
    else toast = getString(R.string.toast_item_renamed, pi.getSafeDesc(), desc);

    UIUtils.showToast(this, toast);

    mCurrentFragment.clearSelection();
    refreshList();

    app.setUpdatedList();
  }

  @Override
  public void onDeletePostalItems(ArrayList<PostalItem> piList) {
    final int listSize = piList.size();

    for (PostalItem pi : piList) app.getDatabaseHelper().deletePostalItem(pi.getCod());

    String message = listSize == 1
      ? getString(R.string.toast_item_deleted, piList.get(0).getSafeDesc())
      : getString(R.string.toast_items_deleted, listSize);
    UIUtils.showToast(this, message);

    mCurrentFragment.clearSelection();
    refreshList();

    // Calling CarteiroApplication.setUpdatedList here is unnecessary,
    // as the PostalListFragment class already does it when (un)archived
    // or deleted item animations are finished
  }

  public void onFavClick(View v) {
    app.getDatabaseHelper().togglePostalItemFav((String) v.getTag());
    refreshList();  // TODO Don't refresh the whole list, just update the item acted on

    app.setUpdatedList();
  }

  public void setDrawerCategoryChecked(int category) {
    mDrawerListAdapter.setSelectedCategory(category);
  }

  private void showCategory(int category) {
    // Only replace fragment if it is a different category
    if (mCurrentFragment == null || mCurrentFragment.getCategory() != category) {
      PostalListFragment newFragment = PostalListFragment.newInstance(category);
      String name = getString(Category.getTitle(category));

      mFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
      FragmentTransaction ft = mFragmentManager
          .beginTransaction()
          .replace(R.id.main_content, newFragment, name);
      if (mCurrentFragment != null && category != Category.ALL) ft.addToBackStack(name);  // Avoid adding empty main container and duplicate "all" category to the back stack
      ft.commit();

      mCurrentFragment = newFragment;
      mMainContainer.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
    }
  }

  private PostalListFragment getCurrentFragment() {
    return (PostalListFragment) mFragmentManager.findFragmentById(R.id.main_content);
  }

  private void updateRefreshStatus() {
    if (CarteiroApplication.state.syncing) mCurrentFragment.setRefreshing();
    else mCurrentFragment.onRefreshComplete();
  }

  public void refreshList() {
    mCurrentFragment.refreshList(false);
  }
}
