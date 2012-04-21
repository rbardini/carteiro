package com.rbardini.carteiro.ui;

import java.util.List;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rbardini.carteiro.PostalRecord;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.PostalUtils.Status;

public class PostalRecordListAdapter extends BaseAdapter {
  private Context context;
  private LayoutInflater inflater;
  private List<PostalRecord> list;

  PostalRecordListAdapter(Context context, List<PostalRecord> list) {
    this.context = context;

    // Cache the LayoutInflate to avoid asking for a new one each time
    inflater = LayoutInflater.from(context);

    this.list = list;
  }

  @Override
  public int getCount() {
    return list.size();
  }

  @Override
  public PostalRecord getItem(int position) {
    return list.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    // A ViewHolder keeps references to children views to avoid unneccessary calls to findViewById() on each row
    ViewHolder holder;

    // When convertView is not null, we can reuse it directly, there is no need to reinflate it
    // We only inflate a new View when the convertView supplied by ListView is null
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.postal_record_item, null);

      // Creates a ViewHolder and store references to the two children views we want to bind data to
      holder = new ViewHolder();
      holder.date = (TextView) convertView.findViewById(R.id.text_postal_record_date);
      holder.time = (TextView) convertView.findViewById(R.id.text_postal_record_time);
      holder.status = (TextView) convertView.findViewById(R.id.text_postal_record_status);
      holder.loc = (TextView) convertView.findViewById(R.id.text_postal_record_loc);
      holder.info = (TextView) convertView.findViewById(R.id.text_postal_record_info);
      holder.icon = (ImageView) convertView.findViewById(R.id.img_postal_record_status);

      convertView.setTag(holder);
    } else {
      // Get the ViewHolder back to get fast access to the TextView and the ImageView
      holder = (ViewHolder) convertView.getTag();
    }

    PostalRecord pr = list.get(position);

    // Bind the data efficiently with the holder
    holder.date.setText(DateUtils.formatDateTime(context, pr.getDate().getTime(), DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_WEEKDAY));
    holder.time.setText(DateUtils.formatDateTime(context, pr.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_12HOUR));
    holder.status.setText(pr.getStatus());
    holder.loc.setText(pr.getLoc());
    String info = pr.getInfo();
    if (info != null) {
      holder.info.setText(info);
      holder.info.setVisibility(View.VISIBLE);
    }
    holder.icon.setImageResource(Status.getIcon(pr.getStatus()));

    return convertView;
  }

  private static class ViewHolder {
    private TextView date, time, status, loc, info;
    private ImageView icon;
  }
}
