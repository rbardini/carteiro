package com.rbardini.carteiro.ui.swipedismiss;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public abstract class SwipeDismissAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  public abstract boolean addItem(int index, @NonNull Object item);
  public abstract void removeItem(@NonNull Object item);
  public abstract Object getItem(int position);
  public abstract List<?> getItems();
}
