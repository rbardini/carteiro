package com.rbardini.carteiro.ui;

import android.content.Context;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.rbardini.carteiro.R;

public class ThemePreference extends DialogPreference implements View.OnClickListener {
  private static final int THEME_CHANGE_DELAY = 250;

  private String mChosenTheme;

  public ThemePreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    setDialogLayoutResource(R.layout.dialog_theme);
    setPositiveButtonText(null);
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    view.findViewById(R.id.theme_light_button).setOnClickListener(this);
    view.findViewById(R.id.theme_dark_button).setOnClickListener(this);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    if (mChosenTheme != null && !mChosenTheme.equals(getValue())) {
      persistString(mChosenTheme);

      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          ProcessPhoenix.triggerRebirth(getContext());
        }
      }, THEME_CHANGE_DELAY);
    }

    super.onDialogClosed(positiveResult);
  }

  @Override
  public void onClick(View v) {
    mChosenTheme = (String) v.getTag();
    getDialog().dismiss();
  }

  String getValue() {
    return getPersistedString(getContext().getString(R.string.theme_light));
  }
}
