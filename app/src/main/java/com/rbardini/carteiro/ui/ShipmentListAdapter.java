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
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

import java.util.List;

public class ShipmentListAdapter extends ArrayAdapter<Shipment> {
  private final Context mContext;
  private final ListView mListView;
  private final LayoutInflater mInflater;

  ShipmentListAdapter(Context context, List<Shipment> list, ListView listView) {
    super(list);

    mContext = context;
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
      convertView = mInflater.inflate(R.layout.list_shipment_item, null);

      // Creates a ViewHolder and store references to the two children views we want to bind data to
      holder = new ViewHolder();
      holder.desc = convertView.findViewById(R.id.text_postal_status_title);
      holder.date = convertView.findViewById(R.id.text_postal_status_date);
      holder.loc = convertView.findViewById(R.id.text_postal_status_loc);
      holder.info = convertView.findViewById(R.id.text_postal_status_info);
      holder.icon = convertView.findViewById(R.id.img_postal_status);
      holder.fav = convertView.findViewById(R.id.star_chkbox);

      convertView.setTag(holder);
    } else {
      // Get the ViewHolder back to get fast access to the TextView and the ImageView
      holder = (ViewHolder) convertView.getTag();
    }

    Shipment shipment = getItem(position);
    ShipmentRecord lastRecord = shipment.getLastRecord();

    // Bind the data efficiently with the holder
    holder.desc.setText(shipment.getDescription());
    holder.date.setText(DateUtils.getRelativeTimeSpanString(lastRecord.getDate().getTime(), System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
    holder.loc.setText(lastRecord.getLocal());
    holder.info.setText(lastRecord.getDescription());
    holder.fav.setChecked(shipment.isFavorite());
    holder.fav.setTag(shipment.getNumber());

    final boolean isChecked = mListView.isItemChecked(position);
    final boolean isUnread = shipment.isUnread();

    // Define postal status icon and background color depending on checked state
    final int iconId = isChecked ? R.drawable.ic_done_white_24dp : Status.getIcon(lastRecord.getStatus());
    final int colorId = isChecked ? R.color.theme_accent_dark : UIUtils.getPostalStatusColor(lastRecord.getStatus());

    holder.icon.setTag(R.id.shipment_icon, iconId);
    holder.icon.setImageResource(iconId);

    final int color = ContextCompat.getColor(mContext, colorId);
    holder.icon.setTag(R.id.shipment_color, color);
    ((GradientDrawable) holder.icon.getBackground()).setColor(color);

    // Add icon click listener to change item checked state
    holder.icon.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mListView.setItemChecked(position, !isChecked);
      }
    });

    // Apply highlight if it is an unread item
    Typeface typeface = Typeface.create(holder.desc.getTypeface(), isUnread ? Typeface.BOLD : Typeface.NORMAL);
    holder.desc.setTypeface(typeface);
    holder.info.setTypeface(typeface);

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