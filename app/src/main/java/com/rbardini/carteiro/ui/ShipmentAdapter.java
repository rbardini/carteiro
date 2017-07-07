package com.rbardini.carteiro.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

public class ShipmentAdapter extends BaseAdapter {
  private final Context mContext;
  private final Shipment mShipment;
  private final LayoutInflater mInflater;

  ShipmentAdapter(Context context, Shipment shipment) {
    mContext = context;
    mShipment = shipment;

    // Cache the LayoutInflate to avoid asking for a new one each time
    mInflater = LayoutInflater.from(context);
  }

  @Override
  public int getCount() {
    return mShipment.size();
  }

  @Override
  public ShipmentRecord getItem(int position) {
    return mShipment.getRecord(mShipment.size() - position - 1);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unnecessary calls to findViewById() on each row
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to re-inflate it
    // We only inflate a new View when the convertView supplied by ListView is null
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.list_record_item, parent, false);

      // Creates a ViewHolder and store references to the two children views we want to bind data to
      holder = new ViewHolder();
      holder.timeline = convertView.findViewById(R.id.timeline);
      holder.date = convertView.findViewById(R.id.text_postal_status_date);
      holder.time = convertView.findViewById(R.id.text_postal_status_time);
      holder.status = convertView.findViewById(R.id.text_postal_status_title);
      holder.loc = convertView.findViewById(R.id.text_postal_status_loc);
      holder.info = convertView.findViewById(R.id.text_postal_status_info);
      holder.icon = convertView.findViewById(R.id.img_postal_status);

      convertView.setTag(holder);
    } else {
      // Get the ViewHolder back to get fast access to the TextView and the ImageView
      holder = (ViewHolder) convertView.getTag();
    }

    ShipmentRecord record = getItem(position);

    // Bind the data efficiently with the holder
    holder.date.setText(DateUtils.formatDateTime(mContext, record.getDate().getTime(), DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_ABBREV_ALL));
    holder.time.setText(DateUtils.formatDateTime(mContext, record.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_ABBREV_ALL));
    holder.status.setText(record.getStatus());
    holder.loc.setText(record.getLocal());
    String info = record.getInfo();
    holder.info.setText(info);
    holder.info.setVisibility(info != null ? View.VISIBLE : View.GONE);
    holder.icon.setImageResource(Status.getIcon(record.getStatus()));

    // Set postal status icon background color
    ((GradientDrawable) holder.icon.getBackground()).setColor(ContextCompat.getColor(mContext, UIUtils.getPostalStatusColor(record.getStatus())));

    // Clip timeline ends
    if (mShipment.size() <= 1) {
      holder.timeline.setBackgroundResource(0);

    } else {
      if (position == 0) {
        holder.timeline.setBackgroundResource(R.drawable.timeline_top);

      } else if (position == mShipment.size() - 1) {
        holder.timeline.setBackgroundResource(R.drawable.timeline_bottom);

      } else {
        holder.timeline.setBackgroundResource(R.drawable.timeline_middle);
      }
    }

    return convertView;
  }

  private static class ViewHolder {
    private View timeline;
    private TextView date, time, status, loc, info;
    private ImageView icon;
  }
}
