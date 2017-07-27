package com.rbardini.carteiro.ui.swipedismiss;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SwipeDismissHandler implements SwipeableRecyclerView.RecyclerViewSwipeListener {
  public static final int UNDO_TIMEOUT = 4000;

  private Snackbar mSnackbar;
  private View mRootView;
  private Handler mUiHandler;
  private int mSingleTextResId;
  private int mMultipleTextResId;
  private int mActionTextResId;
  private SwipeDismissAdapter mAdapter;
  private SwipeDismissListener mListener;

  private ConcurrentSkipListMap<Integer, Object> mDismissQueue = new ConcurrentSkipListMap<>();
  private ConcurrentSkipListMap<Integer, Object> mRestoreQueue = new ConcurrentSkipListMap<>();
  private final Runnable mHandlerCallback = new Runnable() {
    @Override
    public void run() {
      TreeMap<Integer, Object> mDismissQueueCopy = new TreeMap<>(mDismissQueue);

      mListener.onItemsDismissed(mDismissQueueCopy);
      setUpDismissQueue();

      if (mSnackbar != null) {
        mSnackbar.dismiss();
        mSnackbar = null;
      }
    }
  };

  public <T extends SwipeDismissAdapter> SwipeDismissHandler(@NonNull View view, @NonNull Handler uiHandler,
      @StringRes int singleTextResId, @StringRes int multipleTextResId, @StringRes int actionTextResId,
      @NonNull T adapter, @NonNull SwipeDismissListener listener) {
    mRootView = view;
    mUiHandler = uiHandler;
    mSingleTextResId = singleTextResId;
    mMultipleTextResId = multipleTextResId;
    mActionTextResId = actionTextResId;
    mAdapter = adapter;
    mListener = listener;

    setUpDismissQueue();
    setUpRestoreQueue(mAdapter.getItems());
  }

  @Override
  public void onSwipe(int position) {
    dismiss(position);
  }

  public void dismiss(final int currentPosition) {
    Object item = addToDismissQueue(currentPosition);
    mAdapter.removeItem(item);

    notifyUser(mRootView);
  }

  public void dismiss(final int[] currentPositions) {
    Object[] items = new Object[currentPositions.length];

    for (int i = 0; i < currentPositions.length; i++) {
      items[i] = addToDismissQueue(currentPositions[i]);
    }

    for (Object item : items) {
      mAdapter.removeItem(item);
    }

    notifyUser(mRootView);
  }

  public void finish() {
    if (mDismissQueue.size() > 0) {
      mListener.onItemsDismissed(mDismissQueue);
    }

    if (mSnackbar != null) {
      mSnackbar.dismiss();
      mSnackbar = null;
    }
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

  private void notifyUser(final View view) {
    Context context = view.getContext();
    int dismissQueueSize = mDismissQueue.size();
    String text = context.getString(dismissQueueSize > 1 ? mMultipleTextResId : mSingleTextResId, dismissQueueSize);
    String actionText = context.getString(mActionTextResId);

    mUiHandler.removeCallbacks(mHandlerCallback);

    if (mSnackbar == null) {
      mSnackbar = Snackbar
        .make(view, text, Snackbar.LENGTH_INDEFINITE)
        .setAction(actionText, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mUiHandler.removeCallbacks(mHandlerCallback);

            for (Map.Entry<Integer, Object> entry : mDismissQueue.entrySet()) {
              int insertIndex = getInitialListPosition(entry.getValue());
              mAdapter.addItem(insertIndex, entry.getValue());
            }

            setUpDismissQueue();
            mSnackbar = null;
          }
        });
      mSnackbar.show();

    } else {
      mSnackbar.setText(text);
    }

    mUiHandler.postDelayed(mHandlerCallback, UNDO_TIMEOUT);
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
