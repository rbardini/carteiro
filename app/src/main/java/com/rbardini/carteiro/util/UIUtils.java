package com.rbardini.carteiro.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Toast;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.ui.MainActivity;

import java.util.Date;

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

  public static void showToast(Context context, int resId) {
    showToast(context, context.getString(resId));
  }

  public static CharSequence getRelativeTime(Date date) {
    return DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
  }

  public static CharSequence getRelativeDaysString(Context context, Date date) {
    int days = (int) ((System.currentTimeMillis() - date.getTime()) / (1000 * 60 * 60 * 24));

    if (days == 0) return context.getString(R.string.date_today);
    if (days == 1) return context.getString(R.string.date_yesterday);
    if (days < 365) return context.getString(R.string.date_relative_days_ago, days);
    return context.getString(R.string.date_relative_over_year);
  }

  public static void shareItem(Context context, PostalItem pi) {
    Intent shareIntent = PostalUtils.getShareIntent(context, pi);
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)));
  }

  public static void locateItem(Context context, PostalItem pi) throws Exception {
    if (pi.getLoc() == null) throw new Exception(context.getString(R.string.text_unknown_location));

    Intent locateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PostalUtils.getLocation(pi, true)));
    context.startActivity(locateIntent);
  }

  public static int getStatusBarHeight(Context context) {
    Resources res = context.getResources();
    int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");

    return resourceId > 0 ? res.getDimensionPixelSize(resourceId) : 0;
  }

  public static void addStatusBarPadding(Activity activity, int rootLayoutId) {
    int paddingTop = getStatusBarHeight(activity);
    View rootLayout = activity.findViewById(rootLayoutId);

    rootLayout.setPadding(rootLayout.getPaddingLeft(), rootLayout.getPaddingTop() + paddingTop, rootLayout.getPaddingRight(), rootLayout.getPaddingBottom());
  }

  public static int getPostalStatusColor(String status) {
    int category = PostalUtils.Status.getCategory(status);
    return PostalUtils.Category.getColor(category == 0 ? PostalUtils.Category.ALL : category);
  }

  public static Bitmap createScaledDpBitmap(Context context, Bitmap src, int dstWidthDp, int dstHeightDp) {
    final float density = context.getResources().getDisplayMetrics().density;
    final int dstWidthPx = (int) (dstWidthDp * density + 0.5f);
    final int dstHeightPx = (int) (dstHeightDp * density + 0.5f);

    return Bitmap.createScaledBitmap(src, dstWidthPx, dstHeightPx, true);
  }
}
