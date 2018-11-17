package com.rbardini.carteiro.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncTask;
import com.rbardini.carteiro.ui.transition.RoundIconTransition;
import com.rbardini.carteiro.util.NotificationUtils;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.SyncUtils;
import com.rbardini.carteiro.util.UIUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.rbardini.carteiro.ui.RecordActivity.ACTION_DELETE;
import static com.rbardini.carteiro.ui.RecordActivity.EXTRA_SHIPMENT;

public class MainActivity extends ShipmentActivity {
  // Delay to launch navigation drawer item, to allow close animation to play
  private static final int NAVDRAWER_LAUNCH_DELAY = 250;

  private SharedPreferences mPrefs;
  private FragmentManager mFragmentManager;
  private ShipmentListFragment mCurrentFragment;
  private Handler mHandler;
  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private NavigationView mNavigationView;
  private TextView mLastSyncNotice;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mFragmentManager = getSupportFragmentManager();
    mHandler = new Handler();

    setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    setupNavigationDrawer();
    setupAddButton();
    setupFragment(savedInstanceState);

    NotificationUtils.createNotificationChannels(this);
    syncOnLaunchIfEnabled();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    setIntent(intent);
    handleNewIntent();
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
    getSupportActionBar().setTitle(title);
  }

  @Override
  public void onBackPressed() {
    if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
      mDrawerLayout.closeDrawers();
      return;
    }

    int initialCategory = getInitialCategory();
    if (!mCurrentFragment.hasSelection() && mCurrentFragment.getCategory() != initialCategory) {
      showCategory(initialCategory);
      return;
    }

    super.onBackPressed();
  }

  @Override
  public void onSyncStatusChange(Intent intent) {
    super.onSyncStatusChange(intent);
    updateLastSyncNotice();
  }

  @Override
  public void onPostalListAttached(ShipmentListFragment f) {
    int category = f.getCategory();

    setTitle(Category.getTitle(category));
    setDrawerCategoryChecked(category);
  }

  @Override
  public ShipmentFragment getPostalFragment() {
    return mCurrentFragment;
  }

  private void setDrawerCategoryChecked(int category) {
    MenuItem menuItem = mNavigationView.getMenu().findItem(Category.getId(category));
    menuItem.setChecked(true);
  }

  private void setupAddButton() {
    final FloatingActionButton fab = findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this, AddActivity.class);

        RoundIconTransition.addExtras(intent, ContextCompat.getColor(MainActivity.this, R.color.theme_accent), R.drawable.ic_add_white_24dp, 1);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this, fab, getString(R.string.transition_add_item));

        startActivity(intent, options.toBundle());
      }
    });
  }

  private void setupFragment(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      mCurrentFragment = (ShipmentListFragment) mFragmentManager.findFragmentById(R.id.main_content);
      return;
    }

    handleNewIntent();
  }

  private void handleNewIntent() {
    final Intent intent = getIntent();
    final Bundle extras = intent.getExtras();
    final String action = intent.getAction();
    Shipment shipment = null;

    if (extras != null) {
      shipment = (Shipment) extras.getSerializable(EXTRA_SHIPMENT);
      intent.removeExtra(EXTRA_SHIPMENT);
    }

    switch (action) {
      case ACTION_DELETE:
        showCategory(shipment.isArchived() ? Category.ARCHIVED : Category.ALL, shipment);
        break;

      default:
        showCategory(getInitialCategory());
    }
  }

  private void setupNavigationDrawer() {
    mDrawerLayout = findViewById(R.id.drawer_layout);
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
      @Override
      public void onDrawerOpened(View view) {
        super.onDrawerOpened(view);
        updateLastSyncNotice();
      }
    };
    mDrawerLayout.addDrawerListener(mDrawerToggle);

    mNavigationView = findViewById(R.id.navigation_view);
    mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        final int id = menuItem.getItemId();

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

              case R.id.action_contact:
                UIUtils.openURL(MainActivity.this, getString(R.string.contact_url));
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

    mLastSyncNotice = mNavigationView.getHeaderView(0).findViewById(R.id.last_sync_notice);
    mLastSyncNotice.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SyncTask.run(app, mCurrentFragment.getList());
      }
    });
    updateLastSyncNotice();
  }

  private void syncOnLaunchIfEnabled() {
    if (mPrefs.getBoolean(getString(R.string.pref_key_sync_on_launch), true)) {
      SyncTask.run(app, SyncUtils.getShipmentsForSync(app));
    }
  }

  private void updateLastSyncNotice() {
    if (mLastSyncNotice == null) return;

    long lastSyncTimestamp = PreferenceManager.getDefaultSharedPreferences(this).getLong(getString(R.string.pref_key_last_sync), 0);

    if (lastSyncTimestamp == 0) {
      mLastSyncNotice.setVisibility(View.GONE);
      return;
    }

    mLastSyncNotice.setVisibility(View.VISIBLE);

    if (CarteiroApplication.syncing) {
      mLastSyncNotice.setText(R.string.last_sync_notice_syncing);
      return;
    }

    CharSequence lastSyncRelative = UIUtils.getRelativeMinutesString(this, lastSyncTimestamp);
    mLastSyncNotice.setText(getString(R.string.last_sync_notice_synced, lastSyncRelative.toString().toLowerCase()));
  }

  private void showCategory(int category, Shipment shipmentToDelete) {
    final boolean isDifferentCategory = mCurrentFragment == null || mCurrentFragment.getCategory() != category;
    final boolean hasShipmentToDelete = shipmentToDelete != null;

    if (isDifferentCategory || hasShipmentToDelete) {
      ShipmentListFragment newFragment = ShipmentListFragment.newInstance(category, shipmentToDelete);
      String name = getString(Category.getTitle(category));

      mFragmentManager
        .beginTransaction()
        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        .replace(R.id.main_content, newFragment, name)
        .commitAllowingStateLoss();

      mCurrentFragment = newFragment;
    }
  }

  private void showCategory(int category) {
    showCategory(category, null);
  }

  private int getInitialCategory() {
    return Integer.parseInt(mPrefs.getString(getString(R.string.pref_key_initial_category), String.valueOf(Category.ALL)));
  }
}
