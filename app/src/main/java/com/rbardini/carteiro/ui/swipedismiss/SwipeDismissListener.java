package com.rbardini.carteiro.ui.swipedismiss;

import java.util.Map;

import androidx.annotation.NonNull;

public interface SwipeDismissListener {
  void onItemClicked(@NonNull Object item);
  void onItemSelected(@NonNull Object item);
  void onItemsDismissed(@NonNull final Map<Integer, Object> items);
}
