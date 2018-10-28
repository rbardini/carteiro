package com.rbardini.carteiro.ui.swipedismiss;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class SwipeDismissAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  public abstract boolean addItem(int index, @NonNull Object item);
  public abstract void removeItem(@NonNull Object item);
  public abstract Object getItem(int position);
  public abstract List<?> getItems();
}
