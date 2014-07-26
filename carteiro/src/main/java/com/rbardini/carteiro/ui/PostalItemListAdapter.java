package com.rbardini.carteiro.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.util.PostalUtils.Status;

import java.util.List;
import java.util.Set;

public class PostalItemListAdapter extends ArrayAdapter<PostalItem> {
  private final Set<String> mUpdatedCods;
  private final LayoutInflater mInflater;

  PostalItemListAdapter(Context context, List<PostalItem> list, Set<String> updatedCods) {
    super(list);

    mUpdatedCods = updatedCods;

    // Cache the LayoutInflate to avoid asking for a new one each time
    mInflater = LayoutInflater.from(context);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unneccessary calls to findViewById() on each row
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to reinflate it
    // We only inflate a new View when the convertView supplied by ListView is null
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.postal_list_item, null);

      // Creates a ViewHolder and store references to the two children views we want to bind data to
      holder = new ViewHolder();
      holder.cod = (TextView) convertView.findViewById(R.id.text_postal_item_cod);
      holder.desc = (TextView) convertView.findViewById(R.id.text_postal_item_desc);
      holder.date = (TextView) convertView.findViewById(R.id.text_postal_item_date);
      holder.loc = (TextView) convertView.findViewById(R.id.text_postal_item_loc);
      holder.info = (TextView) convertView.findViewById(R.id.text_postal_item_info);
      holder.icon = (ImageView) convertView.findViewById(R.id.img_postal_item_status);
      holder.fav = (CheckBox) convertView.findViewById(R.id.star_chkbox);

      convertView.setTag(holder);
    } else {
      // Get the ViewHolder back to get fast access to the TextView and the ImageView
      holder = (ViewHolder) convertView.getTag();
    }

    PostalItem pi = getItem(position);

    // Bind the data efficiently with the holder
    holder.cod.setText(pi.getCod());
    holder.desc.setText(pi.getSafeDesc());
    holder.date.setText(DateUtils.getRelativeTimeSpanString(pi.getDate().getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS));
    holder.loc.setText(pi.getLoc());
    holder.info.setText(pi.getFullInfo());
    holder.icon.setImageResource(Status.getIcon(pi.getStatus()));
    holder.fav.setChecked(pi.isFav());
    holder.fav.setTag(pi.getCod());

    if (mUpdatedCods != null) {
      convertView.setBackgroundResource(mUpdatedCods.contains(pi.getCod()) ? R.drawable.list_item_highlight : R.drawable.list_item_background);
    }

    return convertView;
  }

  @Override
  public long getItemId(final int position) {
    return getItem(position).hashCode();
  }

  private static class ViewHolder {
    private TextView cod, desc, date, loc, info;
    private ImageView icon;
    private CheckBox fav;
  }
}
