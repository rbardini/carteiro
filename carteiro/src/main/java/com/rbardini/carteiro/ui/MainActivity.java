package com.rbardini.carteiro.ui;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

public class MainActivity extends PostalActivity {
  // Delay to launch navigation drawer item, to allow close animation to play
  private static final int NAVDRAWER_LAUNCH_DELAY = 250;

  // Fade in and fade out durations for the activity_main content when switching between
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
  private ActionBarDrawerToggle mDrawerToggle;
  private NavigationView mNavigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mFragmentManager = getFragmentManager();
    mNotificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
    mHandler = new Handler();

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    mActionBar = getSupportActionBar();
    mActionBar.setHomeButtonEnabled(true);
    mActionBar.setDisplayHomeAsUpEnabled(true);

    mMainContainer = findViewById(R.id.main_content);

    setupNavigationDrawer();
    setupAddButton();

    if (savedInstanceState == null) {
      showCategory(Category.ALL);
    } else {
      mCurrentFragment = getCurrentFragment();
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
          mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
          mDrawerLayout.openDrawer(GravityCompat.START);
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
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawers();
      return;
    }

    if (!mCurrentFragment.hasSelection() && mCurrentFragment.getCategory() != Category.ALL) {
      showCategory(Category.ALL);
      return;
    }

    super.onBackPressed();
  }

  @Override
  public void onPostalListAttached(PostalListFragment f) {
    int category = f.getCategory();

    setTitle(Category.getTitle(category));
    setDrawerCategoryChecked(category);
  }

  @Override
  public PostalFragment getPostalFragment() {
    return mCurrentFragment;
  }

  private void setDrawerCategoryChecked(int category) {
    MenuItem menuItem = mNavigationView.getMenu().findItem(Category.getId(category));
    menuItem.setChecked(true);
  }

  private void setupAddButton() {
    FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.fab);
    addButton.setOnClickListener(new View.OnClickListener() {
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
  }

  private void setupNavigationDrawer() {
    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
    mDrawerLayout.setDrawerListener(mDrawerToggle);
    mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

    mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
    mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(final MenuItem menuItem) {
        final int id = menuItem.getItemId();
        final boolean isCategory = id != R.id.action_preferences && id != R.id.action_feedback;

        // Fade out main container if a new category fragment will be shown
        if (isCategory && Category.getCategoryById(id) != mCurrentFragment.getCategory()) {
          mMainContainer.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
        }

        // Launch item after a short delay, to allow navigation drawer close animation to play
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            switch (id) {
              case R.id.action_preferences:
                startActivity(new Intent(MainActivity.this, PreferencesActivity.class));
                break;

              case R.id.action_feedback:
                UIUtils.openURL(MainActivity.this, getString(R.string.feedback_url));
                break;

              default:
                mCurrentFragment.clearSelection();
                showCategory(Category.getCategoryById(id));
            }
          }
        }, NAVDRAWER_LAUNCH_DELAY);

        mDrawerLayout.closeDrawers();
        return true;
      }
    });
  }

  private void showCategory(int category) {
    // Only replace fragment if it is a different category
    if (mCurrentFragment == null || mCurrentFragment.getCategory() != category) {
      PostalListFragment newFragment = PostalListFragment.newInstance(category);
      String name = getString(Category.getTitle(category));

      FragmentTransaction ft = mFragmentManager
        .beginTransaction()
        .replace(R.id.main_content, newFragment, name);
      ft.commit();

      mCurrentFragment = newFragment;
      mMainContainer.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
    }
  }

  private PostalListFragment getCurrentFragment() {
    return (PostalListFragment) mFragmentManager.findFragmentById(R.id.main_content);
  }
}
