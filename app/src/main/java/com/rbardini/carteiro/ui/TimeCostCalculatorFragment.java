package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;

public class TimeCostCalculatorFragment extends Fragment {
  protected static final String TAG = "TimeCostCalculatorFragment";

  private CarteiroApplication app;
  private Activity activity;

  public static TimeCostCalculatorFragment newInstance() {
    TimeCostCalculatorFragment f = new TimeCostCalculatorFragment();
    return f;
  }

  @Override @SuppressWarnings("unchecked")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = getActivity();
    app = (CarteiroApplication) activity.getApplication();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.time_cost_calculator, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
  }
}
