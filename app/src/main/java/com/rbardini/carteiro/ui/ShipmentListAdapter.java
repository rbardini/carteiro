package com.rbardini.carteiro.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;
import com.rbardini.carteiro.ui.swipedismiss.SwipeDismissAdapter;
import com.rbardini.carteiro.ui.swipedismiss.SwipeDismissListener;
import com.rbardini.carteiro.ui.swipedismiss.SwipeableRecyclerView;
import com.rbardini.carteiro.util.PostalUtils.Status;
import com.rbardini.carteiro.util.UIUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class ShipmentListAdapter extends SwipeDismissAdapter {
  private final Context mContext;
  private final List<Shipment> mShipments;
  private final List<Shipment> mSelectedShipments;
  private final SwipeDismissListener mListener;
  private final LayoutInflater mInflater;

  ShipmentListAdapter(Context context, List<Shipment> shipments, List<Shipment> selectedShipments, SwipeDismissListener listener) {
    mContext = context;
    mShipments = shipments;
    mSelectedShipments = selectedShipments;
    mListener = listener;

    // Cache the LayoutInflater to avoid asking for a new one each time
    mInflater = LayoutInflater.from(context);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(mInflater.inflate(R.layout.list_shipment_item, parent, false));
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    final Shipment shipment = getItem(position);
    final ShipmentRecord lastRecord = shipment.getLastRecord();
    final ViewHolder viewHolder = (ViewHolder) holder;

    // Bind the data efficiently with the holder
    viewHolder.desc.setText(shipment.getDescription());
    viewHolder.date.setText(DateUtils.getRelativeTimeSpanString(lastRecord.getDate().getTime(), System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
    viewHolder.loc.setText(lastRecord.getLocal());
    viewHolder.info.setText(lastRecord.getDescription());
    viewHolder.fav.setChecked(shipment.isFavorite());
    viewHolder.fav.setTag(shipment.getNumber());

    final boolean isChecked = mSelectedShipments.contains(shipment);
    final boolean isUnread = shipment.isUnread();

    // Define postal status icon and background color depending on checked state
    final int iconId = isChecked ? R.drawable.ic_done_white_24dp : Status.getIcon(lastRecord.getStatus());
    final int colorId = isChecked ? R.color.theme_accent_dark : UIUtils.getPostalStatusColor(lastRecord.getStatus());

    viewHolder.itemView.setActivated(isChecked);
    viewHolder.icon.setTag(R.id.shipment_icon, iconId);
    viewHolder.icon.setImageResource(iconId);

    final int color = ContextCompat.getColor(mContext, colorId);
    viewHolder.icon.setTag(R.id.shipment_color, color);
    ((GradientDrawable) viewHolder.icon.getBackground()).setColor(color);

    // Apply highlight if it is an unread item
    Typeface typeface = Typeface.create(viewHolder.desc.getTypeface(), isUnread ? Typeface.BOLD : Typeface.NORMAL);
    viewHolder.desc.setTypeface(typeface);
    viewHolder.info.setTypeface(typeface);
  }

  @Override
  public int getItemCount() {
    return mShipments.size();
  }

  @Override
  public boolean addItem(int index, @NonNull Object item) {
    if (item instanceof Shipment) {
      int adjustedIndex = Math.max(index < mShipments.size() ? index : mShipments.size(), 0);

      mShipments.add(adjustedIndex, (Shipment) item);
      notifyItemInserted(adjustedIndex);

      return true;
    }

    return false;
  }

  @Override
  public void removeItem(@NonNull Object item) {
    int position = getItemPosition(item);

    mShipments.remove(item);
    notifyItemRemoved(position);
  }

  @Override
  public Shipment getItem(int position) {
    return mShipments.get(position);
  }

  @Override
  public List<?> getItems() {
    return mShipments;
  }

  public int getItemPosition(@NonNull Object item) {
    return mShipments.indexOf(item);
  }

  public int[] getItemPositions(@NonNull  List<?> items) {
    int[] positions = new int[items.size()];

    for (int i = 0; i < positions.length; i++) {
      positions[i] = getItemPosition(items.get(i));
    }

    return positions;
  }

  final class ViewHolder extends RecyclerView.ViewHolder implements SwipeableRecyclerView.SwipeableViewHolder,
      View.OnClickListener, View.OnLongClickListener {
    private TextView desc, date, loc, info;
    private ImageView icon;
    private CheckBox fav;

    ViewHolder(View itemView) {
      super(itemView);

      desc = itemView.findViewById(R.id.text_postal_status_title);
      date = itemView.findViewById(R.id.text_postal_status_date);
      loc = itemView.findViewById(R.id.text_postal_status_loc);
      info = itemView.findViewById(R.id.text_postal_status_info);
      icon = itemView.findViewById(R.id.img_postal_status);
      fav = itemView.findViewById(R.id.star_chkbox);

      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
      itemView.setLongClickable(true);

      icon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mListener.onItemSelected(ViewHolder.this);
        }
      });
    }

    @Override
    public void onClick(View view) {
      mListener.onItemClicked(this);
    }

    @Override
    public boolean onLongClick(View view) {
      mListener.onItemSelected(this);
      return true;
    }
  }
}
