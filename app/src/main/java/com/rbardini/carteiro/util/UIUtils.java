package com.rbardini.carteiro.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
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

  public static Bitmap getBitmapFromDrawable(Context context, @DrawableRes int drawableId) {
    Drawable drawable = ContextCompat.getDrawable(context, drawableId);

    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();

    } else if (drawable instanceof VectorDrawable) {
      Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
      drawable.draw(canvas);

      return bitmap;

    } else {
      throw new IllegalArgumentException("Unsupported drawable type");
    }
  }

  public static void shareItem(Context context, Shipment shipment) {
    Intent shareIntent = PostalUtils.getShareIntent(context, shipment);
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_title)));
  }

  public static void locateItem(Context context, Shipment shipment) throws Exception {
    ShipmentRecord record = shipment.getLastRecord();
    if (record.getLocal() == null) throw new Exception(context.getString(R.string.text_unknown_location));

    Intent locateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PostalUtils.getLocation(record, true)));
    context.startActivity(locateIntent);
  }

  public static int getPostalStatusColor(String status) {
    int category = PostalUtils.Status.getCategory(status);
    return PostalUtils.Category.getColor(category == 0 ? PostalUtils.Category.ALL : category);
  }
}
