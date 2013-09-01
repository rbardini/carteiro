package com.rbardini.carteiro.ui;

import java.util.Locale;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils.Category;

public class MainPagerAdapter extends FragmentPagerAdapter {
  public static final int[] category = {Category.RETURNED, Category.UNKNOWN, Category.IRREGULAR, Category.ALL,
                      Category.FAVORITES, Category.AVAILABLE, Category.DELIVERED};

  private static String[] titles;
  private static Object currentView;
  private OnPostPageChangeListener mListener;

  public interface OnPostPageChangeListener {
      void onPostPageSelected(int position);
  }

  public MainPagerAdapter(Context context, FragmentManager fm) {
    super(fm);

    titles = new String[] {
      context.getString(R.string.category_returned).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_unknown).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_irregular).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_all).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_favorites).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_available).toUpperCase(Locale.getDefault()),
      context.getString(R.string.category_delivered).toUpperCase(Locale.getDefault())
    };
  }

  @Override
    public CharSequence getPageTitle(int position) {
      return titles[position];
    }

  @Override
  public int getCount() {
    return titles.length;
  }

  @Override
  public Fragment getItem(int position) {
    return PostalListFragment.newInstance(position);
  }

  @Override
  public int getItemPosition(Object object) {
      return POSITION_NONE;
  }

  @Override
  public void setPrimaryItem(ViewGroup container, int position, Object object) {
    currentView = object;

    if (mListener != null) {
      mListener.onPostPageSelected(position);
    }
  }

  public Object getCurrentView() {
    return currentView;
  }

  public void setOnPostPageChangeListener(OnPostPageChangeListener listener) {
    mListener = listener;
  }
}
