package com.rbardini.carteiro.util;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.rbardini.carteiro.ui.MainActivity;

public final class UIUtils {
  public static void goHome(Context context) {
        final Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

  public static void openURL(Context context, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

  public static void openMarket(Context context) {
        openURL(context, "market://details?id="+context.getPackageName());
    }

    public static void showToast(Context context, String msg) {
      Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static CharSequence getRelativeTime(Date date) {
      return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }
}
