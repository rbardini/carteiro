package com.rbardini.carteiro.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils.Category;

import java.util.ArrayList;
import java.util.List;

public class DrawerListAdapter extends BaseAdapter {
  public static final int ACTION_CATEGORY = 0;
  public static final int ACTION_SETTINGS = 1;
  public static final int ACTION_FEEDBACK = 2;

  private static final int TYPE_ITEM   = 0;
  private static final int TYPE_BUTTON = 1;
  private static final int TYPE_COUNT  = TYPE_BUTTON + 1;

  private final Context mContext;
  private final LayoutInflater mInflater;
  private final List<DrawerModel> mItems;

  private int mSelectedCategory;

  public DrawerListAdapter(Context context) {
    mContext = context;
    mInflater = LayoutInflater.from(mContext);
    mItems = new ArrayList<DrawerModel>();
    mSelectedCategory = Category.ALL;

    initialize();
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  @Override
  public DrawerModel getItem(int position) {
    return mItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    DrawerModel model = getItem(position);
    return model.type;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final DrawerModel model = (DrawerModel) getItem(position);
    final int type = getItemViewType(position);
    final int layout;
    final ViewHolder holder;

    if (convertView == null) {
      switch (type) {
        case TYPE_BUTTON:
          layout = R.layout.drawer_button_item;
          break;

        case TYPE_ITEM:
        default:
          layout = R.layout.drawer_list_item;
          break;
      }
      convertView = mInflater.inflate(layout, null);

      holder = new ViewHolder();
      holder.title = (TextView) convertView.findViewById(R.id.title);
      holder.icon = (ImageView) convertView.findViewById(R.id.icon);

      convertView.setTag(holder);

    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    switch (model.action) {
      case ACTION_CATEGORY:
        holder.title.setText(Category.getTitle(model.id));

        boolean isSelected = model.id == mSelectedCategory;
        holder.title.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
        break;

      case ACTION_SETTINGS:
        holder.title.setText(R.string.action_preferences);
        holder.icon.setImageResource(R.drawable.ic_menu_settings);
        break;

      case ACTION_FEEDBACK:
        holder.title.setText(R.string.action_feedback);
        holder.icon.setImageResource(R.drawable.ic_menu_help);
        break;
    }

    return convertView;
  }

  @Override
  public int getViewTypeCount() {
    return TYPE_COUNT;
  }

  public void setSelectedCategory(int category) {
    mSelectedCategory = category;
    notifyDataSetChanged();
  }

  private void initialize() {
    int[] categories = new int[] {
        Category.ALL,       Category.FAVORITES, Category.AVAILABLE,
        Category.DELIVERED, Category.IRREGULAR, Category.UNKNOWN,
        Category.RETURNED,  Category.ARCHIVED
    };

    // Add category entries
    for (int category : categories) {
      mItems.add(new DrawerModel(TYPE_ITEM, ACTION_CATEGORY, category));
    }

    // Add settings and feedback buttons
    mItems.add(new DrawerModel(TYPE_BUTTON, ACTION_SETTINGS, -1));
    mItems.add(new DrawerModel(TYPE_BUTTON, ACTION_FEEDBACK, -1));
  }

  public static class DrawerModel {
    public int type, action, id;

    DrawerModel(int type, int action, int id) {
      this.type = type;
      this.action = action;
      this.id = id;
    }
  }

  private static class ViewHolder {
    private ImageView icon;
    private TextView title;
  }
}
