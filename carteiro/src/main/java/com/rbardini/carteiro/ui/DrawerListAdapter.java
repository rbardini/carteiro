package com.rbardini.carteiro.ui;

import java.util.Locale;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils.Category;

public class DrawerListAdapter extends BaseAdapter implements StickyListHeadersAdapter {
  private final Context mContext;
  private final LayoutInflater mInflater;
  private final int[] mCategories;

  public DrawerListAdapter(Context context, int[] categories) {
    mContext = context;
    mInflater = LayoutInflater.from(mContext);
    mCategories = categories;
  }

  @Override
  public int getCount() {
    return mCategories.length;
  }

  @Override
  public Object getItem(int position) {
    return mCategories[position];
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ItemViewHolder holder;

    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.drawer_list_item, null);

      holder = new ItemViewHolder();
      holder.title = (TextView) convertView.findViewById(R.id.title);
      holder.icon = (ImageView) convertView.findViewById(R.id.icon);

      convertView.setTag(holder);
    } else {
      holder = (ItemViewHolder) convertView.getTag();
    }

    int category = mCategories[position];

    holder.title.setText(Category.getTitle(category));
    holder.icon.setImageResource(Category.getIcon(category));

    return convertView;
  }

  @Override
  public View getHeaderView(int position, View convertView, ViewGroup parent) {
    HeaderViewHolder holder;

    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.drawer_list_header, null);

      holder = new HeaderViewHolder();
      holder.title = (TextView) convertView.findViewById(R.id.title);

      convertView.setTag(holder);
    } else {
      holder = (HeaderViewHolder) convertView.getTag();
    }

    holder.title.setText(mContext.getString(R.string.drawer_header_tracking).toUpperCase(Locale.getDefault()));

    return convertView;
  }

  @Override
  public long getHeaderId(int position) {
    return 0;
  }

  private static class ItemViewHolder {
    private ImageView icon;
    private TextView title;
  }

  private static class HeaderViewHolder {
    private TextView title;
  }
}
