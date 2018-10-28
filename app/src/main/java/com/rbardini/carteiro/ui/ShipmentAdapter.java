package com.rbardini.carteiro.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ViewHolder> {
  private final Context mContext;
  private final Shipment mShipment;
  private final LayoutInflater mInflater;

  ShipmentAdapter(Context context, Shipment shipment) {
    mContext = context;
    mShipment = shipment;

    // Cache the LayoutInflater to avoid asking for a new one each time
    mInflater = LayoutInflater.from(context);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(mInflater.inflate(R.layout.list_record_item, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
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
  }

  @Override
  public int getItemCount() {
    return mShipment.size();
  }

  private ShipmentRecord getItem(int position) {
    return mShipment.getRecord(mShipment.size() - position - 1);
  }

  final class ViewHolder extends RecyclerView.ViewHolder {
    private View timeline;
    private TextView date, time, status, loc, info;
    private ImageView icon;

    ViewHolder(View itemView) {
      super(itemView);

      timeline = itemView.findViewById(R.id.timeline);
      date = itemView.findViewById(R.id.text_postal_status_date);
      time = itemView.findViewById(R.id.text_postal_status_time);
      status = itemView.findViewById(R.id.text_postal_status_title);
      loc = itemView.findViewById(R.id.text_postal_status_loc);
      info = itemView.findViewById(R.id.text_postal_status_info);
      icon = itemView.findViewById(R.id.img_postal_status);
    }
  }
}
