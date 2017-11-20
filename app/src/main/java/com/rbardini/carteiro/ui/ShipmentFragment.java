package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;

public abstract class ShipmentFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
  protected Activity mActivity;
  protected CarteiroApplication app;
  protected DatabaseHelper dh;
  protected SwipeRefreshLayout mSwipeRefreshLayout;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivity = getActivity();
    app = (CarteiroApplication) mActivity.getApplication();
    dh = app.getDatabaseHelper();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mSwipeRefreshLayout = getView().findViewById(R.id.swipe_layout);
    mSwipeRefreshLayout.setOnRefreshListener(this);
    mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_accent, R.color.theme_accent_dark);

    updateRefreshStatus();
  }

  @Override
  public abstract void onRefresh();

  public abstract void refreshList();

  public void updateRefreshStatus() {
    if (mSwipeRefreshLayout == null) return;
    mSwipeRefreshLayout.setRefreshing(CarteiroApplication.syncing);
  }

  public void clearSelection() {}
}
