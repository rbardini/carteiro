package com.rbardini.carteiro.ui;

import java.util.ArrayList;
import java.util.List;
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
  private static PostalListFragment currentView;
  private OnPostPageChangeListener mListener;
  private FragmentManager mFragmentManager;

  public interface OnPostPageChangeListener {
      void onPostPageSelected(int position);
  }

  public MainPagerAdapter(Context context, FragmentManager fm) {
    super(fm);
    mFragmentManager = fm;

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
  public void notifyDataSetChanged() {
    List<PostalListFragment> fragments = this.getActiveFragments();
    for (PostalListFragment fragment : fragments) {
      fragment.refreshList(false);
    }
  }

  @Override
  public void setPrimaryItem(ViewGroup container, int position, Object object) {
    if (object != currentView) {
      currentView = (PostalListFragment) object;

      if (mListener != null) {
        mListener.onPostPageSelected(position);
      }
    }
  }

  public PostalListFragment getCurrentView() {
    return currentView;
  }

  public void setOnPostPageChangeListener(OnPostPageChangeListener listener) {
    mListener = listener;
  }

  public List<PostalListFragment> getActiveFragments() {
    List<PostalListFragment> list = new ArrayList<PostalListFragment>();
    int count = this.getCount();

    for (int i=0; i<count; i++) {
      PostalListFragment fragment = (PostalListFragment) mFragmentManager.findFragmentByTag("android:switcher:" + R.id.postal_list_pager + ":" + i);
      if (fragment != null && fragment.getView() != null) {
        list.add(fragment);
      }
    }

    return list;
  }

  public void setRefreshing() {
    List<PostalListFragment> fragments = this.getActiveFragments();

    for (PostalListFragment fragment : fragments) {
      fragment.setRefreshing();
    }
  }

  public void onRefreshComplete() {
    List<PostalListFragment> fragments = this.getActiveFragments();

    for (PostalListFragment fragment : fragments) {
      fragment.onRefreshComplete();
    }
  }
}
