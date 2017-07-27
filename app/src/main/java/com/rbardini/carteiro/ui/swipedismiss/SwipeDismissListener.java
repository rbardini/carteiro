package com.rbardini.carteiro.ui.swipedismiss;

import android.support.annotation.NonNull;

import java.util.Map;

public interface SwipeDismissListener {
  void onItemClicked(@NonNull Object item);
  void onItemSelected(@NonNull Object item);
  void onItemsDismissed(@NonNull final Map<Integer, Object> items);
}
