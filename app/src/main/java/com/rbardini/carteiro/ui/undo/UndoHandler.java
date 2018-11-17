package com.rbardini.carteiro.ui.undo;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class UndoHandler {
  public static final int UNDO_TIMEOUT = 4000;

  private Snackbar mSnackbar;
  private View mRootView;
  private Handler mUiHandler;
  private int mSingleTextResId;
  private int mMultipleTextResId;
  private int mActionTextResId;
  private int mCount;
  private UndoListener mListener;

  private final Runnable mHandlerCallback = new Runnable() {
    @Override
    public void run() {
      mListener.onDismiss(mCount);
      resetCount();

      if (mSnackbar != null) {
        mSnackbar.dismiss();
        mSnackbar = null;
      }
    }
  };

  public UndoHandler(@NonNull View view, @NonNull Handler uiHandler, @StringRes int singleTextResId,
      @StringRes int multipleTextResId, @StringRes int actionTextResId, @NonNull UndoListener listener) {
    mRootView = view;
    mUiHandler = uiHandler;
    mSingleTextResId = singleTextResId;
    mMultipleTextResId = multipleTextResId;
    mActionTextResId = actionTextResId;
    mListener = listener;

    resetCount();
  }

  public void dismiss(final int count) {
    addToCount(count);
    notifyUser();
  }

  public void finish() {
    mListener.onDismiss(mCount);

    if (mSnackbar != null) {
      mSnackbar.dismiss();
      mSnackbar = null;
    }
  }

  private void resetCount() {
    mCount = 0;
  }

  private void addToCount(int count) {
    mCount += count;
  }

  private void notifyUser() {
    Context context = mRootView.getContext();
    String text = context.getString(mCount > 1 ? mMultipleTextResId : mSingleTextResId, mCount);
    String actionText = context.getString(mActionTextResId);

    mUiHandler.removeCallbacks(mHandlerCallback);

    if (mSnackbar == null) {
      mSnackbar = Snackbar
        .make(mRootView, text, Snackbar.LENGTH_INDEFINITE)
        .setAction(actionText, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            mUiHandler.removeCallbacks(mHandlerCallback);
            mListener.onUndo(mCount);

            resetCount();
            mSnackbar = null;
          }
        });
      mSnackbar.show();

    } else {
      mSnackbar.setText(text);
    }

    mUiHandler.postDelayed(mHandlerCallback, UNDO_TIMEOUT);
  }
}
