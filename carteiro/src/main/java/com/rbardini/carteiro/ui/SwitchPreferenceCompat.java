package com.rbardini.carteiro.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;

import com.rbardini.carteiro.R;

public class SwitchPreferenceCompat extends CheckBoxPreference {
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public SwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  public SwitchPreferenceCompat(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public SwitchPreferenceCompat(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SwitchPreferenceCompat(Context context) {
    super(context);
    init();
  }

  private void init() {
    setWidgetLayoutResource(R.layout.switch_preference);
  }
}
