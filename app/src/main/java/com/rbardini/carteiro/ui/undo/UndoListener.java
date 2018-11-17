package com.rbardini.carteiro.ui.undo;

import androidx.annotation.NonNull;

public interface UndoListener {
  void onDismiss(@NonNull int count);
  void onUndo(@NonNull int count);
}
