package com.rbardini.carteiro.ui.swipedismiss;

import android.os.Handler;
import android.view.View;

import com.rbardini.carteiro.ui.undo.UndoHandler;
import com.rbardini.carteiro.ui.undo.UndoListener;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class SwipeDismissHandler implements SwipeableRecyclerView.RecyclerViewSwipeListener, UndoListener {
  private SwipeDismissAdapter mAdapter;
  private SwipeDismissListener mListener;
  private UndoHandler mHandler;

  private ConcurrentSkipListMap<Integer, Object> mDismissQueue = new ConcurrentSkipListMap<>();
  private ConcurrentSkipListMap<Integer, Object> mRestoreQueue = new ConcurrentSkipListMap<>();

  public <T extends SwipeDismissAdapter> SwipeDismissHandler(@NonNull View view, @NonNull Handler uiHandler,
      @StringRes int singleTextResId, @StringRes int multipleTextResId, @StringRes int actionTextResId,
      @NonNull T adapter, @NonNull SwipeDismissListener listener) {
    mAdapter = adapter;
    mListener = listener;
    mHandler = new UndoHandler(view, uiHandler, singleTextResId, multipleTextResId, actionTextResId, this);

    setUpDismissQueue();
    setUpRestoreQueue(mAdapter.getItems());
  }

  @Override
  public void onSwipe(int position) {
    dismiss(position);
  }

  @Override
  public void onDismiss(@NonNull int count) {
    TreeMap<Integer, Object> mDismissQueueCopy = new TreeMap<>(mDismissQueue);

    mListener.onItemsDismissed(mDismissQueueCopy);
    setUpDismissQueue();
  }

  @Override
  public void onUndo(@NonNull int count) {
    for (Map.Entry<Integer, Object> entry : mDismissQueue.entrySet()) {
      int insertIndex = getInitialListPosition(entry.getValue());
      mAdapter.addItem(insertIndex, entry.getValue());
    }

    setUpDismissQueue();
  }

  public void dismiss(final int currentPosition) {
    Object item = addToDismissQueue(currentPosition);
    mAdapter.removeItem(item);

    mHandler.dismiss(1);
  }

  public void dismiss(final int[] currentPositions) {
    int count = currentPositions.length;
    Object[] items = new Object[count];

    for (int i = 0; i < count; i++) {
      items[i] = addToDismissQueue(currentPositions[i]);
    }

    for (Object item : items) {
      mAdapter.removeItem(item);
    }

    mHandler.dismiss(count);
  }

  public void finish() {
    mHandler.finish();
  }

  private void setUpDismissQueue() {
    mDismissQueue.clear();
  }

  private void setUpRestoreQueue(@Nullable final List<?> data) {
    mRestoreQueue.clear();

    if (data == null) return;

    for (int i = 0; i < data.size(); i++) {
      mRestoreQueue.put(i, data.get(i));
    }
  }

  private Object addToDismissQueue(int currentPosition) {
    final Object item = mAdapter.getItem(currentPosition);
    int restoreIndex = getInitialListPosition(item);

    mDismissQueue.put(restoreIndex, item);

    return item;
  }

  private int getInitialListPosition(final Object value) {
    for (Map.Entry<Integer, Object> entry : mRestoreQueue.entrySet()) {
      if (value.equals(entry.getValue())) {
        return entry.getKey();
      }
    }

    return Integer.MAX_VALUE;
  }
}
