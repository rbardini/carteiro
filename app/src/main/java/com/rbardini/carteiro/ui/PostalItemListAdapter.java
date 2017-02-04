package com.rbardini.carteiro.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

import java.util.List;
import java.util.Set;

public class PostalItemListAdapter extends ArrayAdapter<PostalItem> {
  private final Context mContext;
  private final Set<String> mUpdatedCods;
  private final ListView mListView;
  private final LayoutInflater mInflater;

  PostalItemListAdapter(Context context, List<PostalItem> list, Set<String> updatedCods, ListView listView) {
    super(list);

    mContext = context;
    mUpdatedCods = updatedCods;
    mListView = listView;

    // Cache the LayoutInflate to avoid asking for a new one each time
    mInflater = LayoutInflater.from(mContext);
  }

  @Override
  public View getView(final int position, View convertView, final ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to re-inflate it
    // We only inflate a new View when the convertView supplied by ListView is null
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.list_postal_item, null);

      // Creates a ViewHolder and store references to the two children views we want to bind data to
      holder = new ViewHolder();
      holder.desc = (TextView) convertView.findViewById(R.id.text_postal_status_title);
      holder.date = (TextView) convertView.findViewById(R.id.text_postal_status_date);
      holder.loc = (TextView) convertView.findViewById(R.id.text_postal_status_loc);
      holder.info = (TextView) convertView.findViewById(R.id.text_postal_status_info);
      holder.icon = (ImageView) convertView.findViewById(R.id.img_postal_status);
      holder.fav = (CheckBox) convertView.findViewById(R.id.star_chkbox);

      convertView.setTag(holder);
    } else {
      // Get the ViewHolder back to get fast access to the TextView and the ImageView
      holder = (ViewHolder) convertView.getTag();
    }

    PostalItem pi = getItem(position);

    // Bind the data efficiently with the holder
    holder.desc.setText(pi.getSafeDesc());
    holder.date.setText(DateUtils.getRelativeTimeSpanString(pi.getDate().getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
    holder.loc.setText(pi.getLoc());
    holder.info.setText(pi.getFullInfo());
    holder.fav.setChecked(pi.isFav());
    holder.fav.setTag(pi.getCod());

    final boolean isChecked = mListView.isItemChecked(position);
    final boolean hasUpdate = mUpdatedCods != null && mUpdatedCods.contains(pi.getCod());

    GradientDrawable iconBackground = (GradientDrawable) holder.icon.getBackground();

    // Set postal status icon and background color depending on checked state
    if (isChecked) {
      holder.icon.setImageResource(R.drawable.ic_done_white_24dp);
      iconBackground.setColor(ContextCompat.getColor(mContext, R.color.theme_accent_dark));

    } else {
      holder.icon.setImageResource(Status.getIcon(pi.getStatus()));
      iconBackground.setColor(ContextCompat.getColor(mContext, UIUtils.getPostalStatusColor(pi.getStatus())));
    }

    // Add icon click listener to change item checked state
    holder.icon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mListView.setItemChecked(position, !isChecked);
      }
    });

    // Apply highlight if it is an updated item
    holder.desc.setTypeface(holder.desc.getTypeface(), hasUpdate ? Typeface.BOLD : Typeface.NORMAL);
    holder.info.setTypeface(holder.desc.getTypeface(), hasUpdate ? Typeface.BOLD : Typeface.NORMAL);

    return convertView;
  }

  @Override
  public long getItemId(final int position) {
    return getItem(position).hashCode();
  }

  private static class ViewHolder {
    private TextView desc, date, loc, info;
    private ImageView icon;
    private CheckBox fav;
  }
}
