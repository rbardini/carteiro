package com.rbardini.carteiro.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.jakewharton.processphoenix.ProcessPhoenix;
import com.rbardini.carteiro.R;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class ThemePreference extends DialogPreference {
  private static final int THEME_CHANGE_DELAY = 250;

  public ThemePreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    setDialogLayoutResource(R.layout.dialog_theme);
    setPositiveButtonText(null);
  }

  public String getValue() {
    return getPersistedString(getContext().getString(R.string.theme_light));
  }

  public static class ThemePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements View.OnClickListener {
    private String mChosenTheme;

    public ThemePreferenceDialogFragmentCompat() {}

    @Override
    protected void onBindDialogView(View view) {
      super.onBindDialogView(view);

      view.findViewById(R.id.theme_light_button).setOnClickListener(this);
      view.findViewById(R.id.theme_dark_button).setOnClickListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
      final ThemePreference pref = getThemePreference();
      final Context context = getContext().getApplicationContext();

      if (mChosenTheme != null && !mChosenTheme.equals(pref.getValue())) {
        pref.persistString(mChosenTheme);

        new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
            ProcessPhoenix.triggerRebirth(context);
          }
        }, THEME_CHANGE_DELAY);
      }
    }

    @Override
    public void onClick(View v) {
      mChosenTheme = (String) v.getTag();
      getDialog().dismiss();
    }

    private ThemePreference getThemePreference() {
      return (ThemePreference) this.getPreference();
    }

  }
}
