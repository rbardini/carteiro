package com.rbardini.carteiro.ui;

import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

@SuppressWarnings("UnusedDeclaration")
public class ContextualSwipeUndoAdapter extends ContextualUndoAdapter {
  private final OnSwipeCallback mOnSwipeCallback;

  public interface OnSwipeCallback {
    public void onSwipe(int position);
  }

  public ContextualSwipeUndoAdapter(BaseAdapter baseAdapter, int undoLayoutId, int undoActionId,
                                    DeleteItemCallback deleteItemCallback, OnSwipeCallback onSwipeCallback) {
    super(baseAdapter, undoLayoutId, undoActionId, deleteItemCallback);
    mOnSwipeCallback = onSwipeCallback;
  }

  public ContextualSwipeUndoAdapter(BaseAdapter baseAdapter, int undoLayoutResId, int undoActionResId,
                                    int autoDeleteTimeMillis, DeleteItemCallback deleteItemCallback, OnSwipeCallback onSwipeCallback) {
    super(baseAdapter, undoLayoutResId, undoActionResId, autoDeleteTimeMillis, deleteItemCallback);
    mOnSwipeCallback = onSwipeCallback;
  }

  public ContextualSwipeUndoAdapter(BaseAdapter baseAdapter, int undoLayoutResId, int undoActionResId,
                                    int autoDeleteTime, int countDownTextViewResId, DeleteItemCallback deleteItemCallback,
                                    CountDownFormatter countDownFormatter, OnSwipeCallback onSwipeCallback) {
    super(baseAdapter, undoLayoutResId, undoActionResId, autoDeleteTime, countDownTextViewResId, deleteItemCallback, countDownFormatter);
    mOnSwipeCallback = onSwipeCallback;
  }

  @Override
  public void onViewSwiped(final long dismissViewItemId, final int dismissPosition) {
    super.onViewSwiped(dismissViewItemId, dismissPosition);
    mOnSwipeCallback.onSwipe(dismissPosition);
  }
}
