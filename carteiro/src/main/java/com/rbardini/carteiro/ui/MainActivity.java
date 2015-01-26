package com.rbardini.carteiro.ui;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

public class MainActivity extends PostalActivity {
  protected static final String TAG = "MainActivity";

  // Delay to launch navigation drawer item, to allow close animation to play
  private static final int NAVDRAWER_LAUNCH_DELAY = 250;

  // Fade in and fade out durations for the main content when switching between
  // different fragments through the navigation drawer
  private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
  private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

  private ActionBar mActionBar;
  private FragmentManager mFragmentManager;
  private PostalListFragment mCurrentFragment;
  private NotificationManager mNotificationManager;
  private Handler mHandler;
  private View mMainContainer;
  private DrawerLayout mDrawerLayout;
  private ListView mDrawerList;
  private DrawerListAdapter mDrawerListAdapter;
  private ActionBarDrawerToggle mDrawerToggle;
  private FloatingActionButton mFAB;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    addStatusBarPadding();

    mFAB = (FloatingActionButton) findViewById(R.id.fab);
    mFAB.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          ActivityOptions scaleAnim = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
          startActivity(intent, scaleAnim.toBundle());

        } else {
          startActivity(intent);
        }
      }
    });

    mFragmentManager = getFragmentManager();
    mFragmentManager.addOnBackStackChangedListener(new OnBackStackChangedListener() {
      @Override
      public void onBackStackChanged() {
        mCurrentFragment = getCurrentFragment();
      }
    });
    mNotificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    mHandler = new Handler();

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    mActionBar = getSupportActionBar();
    mActionBar.setHomeButtonEnabled(true);
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
                mCurrentFragment.clearSelection();
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
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
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
    mNotificationManager.cancel(SyncService.NOTIFICATION_NEW_UPDATE);
  }

  @Override
  protected void onPause() {
    if (!CarteiroApplication.state.syncing) app.clearUpdate();
    super.onPause();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem shareItem = menu.findItem(R.id.share_opt);

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
    mActionBar.setTitle(title);
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
      mDrawerLayout.closeDrawer(mDrawerList);
      return;
    }

    if (mFragmentManager.getBackStackEntryCount() > 0 && !mCurrentFragment.hasSelection()) {
      mFragmentManager.popBackStack();
      return;
    }

    super.onBackPressed();
  }

  @Override
  public void onPostalListAttached(PostalListFragment f) {
    int category = f.getCategory();

    setTitle(Category.getTitle(category));
    setDrawerCategoryChecked(category);
    mFAB.attachToListView(f.getListView());
    mFAB.show();
  }

  @Override
  public PostalFragment getPostalFragment() {
    return mCurrentFragment;
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
}
