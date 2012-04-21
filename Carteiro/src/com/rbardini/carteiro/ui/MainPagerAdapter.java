package com.rbardini.carteiro.ui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.viewpagerindicator.TitleProvider;

public class MainPagerAdapter extends FragmentPagerAdapter implements TitleProvider {
  public static final int[] category = {Category.RETURNED, Category.UNKNOWN, Category.IRREGULAR, Category.ALL,
                      Category.FAVORITES, Category.AVAILABLE, Category.DELIVERED};

  private static String[] titles;
  private static Object currentView;

  public MainPagerAdapter(Context context, FragmentManager fm) {
    super(fm);

    titles = new String[] {
      context.getString(R.string.category_returned).toUpperCase(),
      context.getString(R.string.category_unknown).toUpperCase(),
      context.getString(R.string.category_irregular).toUpperCase(),
      context.getString(R.string.category_all).toUpperCase(),
      context.getString(R.string.category_favorites).toUpperCase(),
      context.getString(R.string.category_available).toUpperCase(),
      context.getString(R.string.category_delivered).toUpperCase()
    };
  }

  @Override
  public String getTitle(int position) {
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
  }

  public Object getCurrentView() {
    return currentView;
  }
}
