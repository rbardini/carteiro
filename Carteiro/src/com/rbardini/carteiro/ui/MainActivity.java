package com.rbardini.carteiro.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.svc.DetachableResultReceiver;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.UIUtils;
import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends SherlockFragmentActivity implements DetachableResultReceiver.Receiver, PostalItemDialogFragment.OnPostalItemChangeListener {
  protected static final String TAG = "MainActivity";
  public static final int ADD_REQUEST = 1;

  private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAubBLpYTT5EF9nwTS72jP5ZLZK+ABgxEVssPr7bfQ1QnL3b/EDBAWEuRFaT34ZPU1yffZ6pCE+8AI5EWFsxIE679dev28gN0yJasgsouoeWdHpM5ZojNyjY7POzockXGgVervGTCZENEtMNuhDDKr1136vuKvKtC91e844r07Zyug0Vs2ye26ZnbKXk2pgnpwmwaop1ejqr5kYED5BDD55mgMY3/7HjwUf2k8IK7PUYUcd45z0CjVhSSYQtctAPBVffhcTd1XZQIf/6MvWFSLLCmf+q/r3B6hVEARTEluR23PtElPgaJkBeBcwJM4TbTIgPWq9V2FDSV5IeoRKnazqwIDAQAB";
  private static final byte[] SALT = new byte[] {74, 73, 116, -105, 115, 29, -59, -53, -11, 105, -75, 117, 76, 15, 82, -50, 71, -109, 125, -50};

  private CarteiroApplication app;
  private FragmentManager fragManager;
  private MainPagerAdapter pagerAdapter;

  private Handler handler;
  private LicenseCheckerCallback licenseCheckerCallback;
    private LicenseChecker licenseChecker;
    private boolean licenseChecked;

  @Override
    public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

    app = (CarteiroApplication) getApplication();
    fragManager = getSupportFragmentManager();
    pagerAdapter = new MainPagerAdapter(this, fragManager);

    ViewPager viewPager = (ViewPager) findViewById(R.id.postal_list_pager);
    TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        viewPager.setAdapter(pagerAdapter);
        indicator.setViewPager(viewPager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageSelected(int position) { updateRefreshStatus(); }

      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

      @Override
      public void onPageScrollStateChanged(int state) {}
    });
        viewPager.setCurrentItem(3);

        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        handler = new Handler();
        licenseCheckerCallback = new CarteiroLicenseCheckerCallback();
        licenseChecker = new LicenseChecker(this, new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(), deviceId)), PUBLIC_KEY);
        try {
          licenseChecked = (Boolean) getLastCustomNonConfigurationInstance();
        } catch (NullPointerException e) {
          licenseChecked = false;
        }
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (!licenseChecked && fragManager.findFragmentByTag(MainDialogFragment.TAG) == null) {
      checkLicense();
    }

    CarteiroApplication.state.receiver.setReceiver(this);
    updateRefreshStatus();
    if (app.hasUpdate()) {
      refreshList();
    }
    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
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
  protected void onDestroy() {
    super.onDestroy();
    licenseChecker.onDestroy();
  }

  @Override
    public Object onRetainCustomNonConfigurationInstance() {
      return licenseChecked;
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
      int listSize = ((PostalListFragment) pagerAdapter.getCurrentView()).getList().size();
      menu.findItem(R.id.share_opt).setEnabled(listSize > 0 ? true : false);
    } catch(Exception e) {}

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

      case R.id.share_opt:
        List<PostalItem> list = ((PostalListFragment) pagerAdapter.getCurrentView()).getList();
        String text = "";
        for (int i=0; i<list.size(); i++) {
          PostalItem pi = list.get(i);
          text += String.format(getString(pi.isFav() ? R.string.text_send_list_line_1_fav : R.string.text_send_list_line_1, pi.getCod()));
          if (pi.getDesc() != null) { text += String.format(getString(R.string.text_send_list_line_2, pi.getDesc())); }
          text += String.format(getString(R.string.text_send_list_line_3, pi.getStatus(), UIUtils.getRelativeTime(pi.getDate())));
        }
        Intent export = new Intent(Intent.ACTION_SEND);
        export.setType("text/plain");
        export.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.subject_send_list));
        export.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(export, getString(R.string.title_send_list)));
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

  private void updateRefreshStatus() {
    List<Fragment> fragments = getViewPagerFragments(pagerAdapter);
    if (CarteiroApplication.state.syncing) {
      for (Fragment f : fragments) {
        ((PostalListFragment) f).setRefreshing();
      }
    } else {
      for (Fragment f : fragments) {
        ((PostalListFragment) f).onRefreshComplete();
      }
    }
  }

  public void refreshList() {
    pagerAdapter.notifyDataSetChanged();
  }

  private List<Fragment> getViewPagerFragments(FragmentPagerAdapter adapter) {
    List<Fragment> list = new ArrayList<Fragment>();
    int count = adapter.getCount();

    for (int i=0; i<count; i++) {
      Fragment fragment = fragManager.findFragmentByTag("android:switcher:"+R.id.postal_list_pager+":"+i);
      if(fragment != null) {
        if(fragment.getView() != null) {
          list.add(fragment);
        }
      }
    }

    return list;
  }

  protected void checkLicense() {
        licenseChecker.checkAccess(licenseCheckerCallback);
    }

    private void licenseChecked(final boolean licensed, final boolean retry) {
        handler.post(new Runnable() {
            @Override
      public void run() {
              try {
                if (!licensed) {
                  MainDialogFragment.newInstance(R.id.license_opt, retry).show(getSupportFragmentManager(), MainDialogFragment.TAG);
                }
                licenseChecked = true;
              } catch (Exception e) { Log.i(TAG, "Dropping license response."); }
            }
        });
    }

  private class CarteiroLicenseCheckerCallback implements LicenseCheckerCallback {
        private static final String TAG = "CarteiroLicenseCheckerCallback";

    @Override
    public void allow(int policyReason) {
            if (isFinishing()) { return; }
            licenseChecked(true, false);
        }

        @Override
    public void dontAllow(int policyReason) {
            if (isFinishing()) { return; }
            licenseChecked(false, policyReason == Policy.RETRY);
        }

        @Override
    public void applicationError(int errorCode) {
          Log.w(TAG, "License check application error: "+errorCode);
            if (isFinishing()) { return; }
            licenseChecked(true, false);
        }
    }
}
