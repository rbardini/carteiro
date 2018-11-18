package com.rbardini.carteiro.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class InsetDividerItemDecoration extends RecyclerView.ItemDecoration {
  public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
  public static final int VERTICAL = LinearLayout.VERTICAL;

  private static final String TAG = "InsetDividerItem";
  private static final int[] ATTRS = new int[]{ android.R.attr.listDivider };

  private Drawable mDivider;
  private int mOrientation;
  private int mInsetStart;
  private int mInsetEnd;

  private final Rect mBounds = new Rect();

  public InsetDividerItemDecoration(Context context, int orientation, @Dimension int insetStart, @Dimension int insetEnd) {
    final TypedArray a = context.obtainStyledAttributes(ATTRS);
    mDivider = a.getDrawable(0);
    if (mDivider == null) {
      Log.w(TAG, "@android:attr/listDivider was not set in the theme used for this "
          + "InsetDividerItemDecoration. Please set that attribute all call setDrawable()");
    }
    a.recycle();
    setOrientation(orientation);
    setInset(insetStart, insetEnd);
  }

  public InsetDividerItemDecoration(Context context, int orientation, int inset) {
    this(context, orientation, inset, inset);
  }

  public InsetDividerItemDecoration(Context context, int orientation) {
    this(context, orientation, 0, 0);
  }

  public void setOrientation(int orientation) {
    if (orientation != HORIZONTAL && orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "Invalid orientation. It should be either HORIZONTAL or VERTICAL");
    }
    mOrientation = orientation;
  }

  public void setInset(int insetStart, int insetEnd) {
    mInsetStart = insetStart;
    mInsetEnd = insetEnd;
  }

  public void setDrawable(@NonNull Drawable drawable) {
    if (drawable == null) {
      throw new IllegalArgumentException("Drawable cannot be null.");
    }
    mDivider = drawable;
  }

  @Override
  public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
    if (parent.getLayoutManager() == null || mDivider == null) {
      return;
    }
    if (mOrientation == VERTICAL) {
      drawVertical(c, parent);
    } else {
      drawHorizontal(c, parent);
    }
  }

  private void drawVertical(Canvas canvas, RecyclerView parent) {
    canvas.save();
    final int left;
    final int right;

    if (parent.getClipToPadding()) {
      left = parent.getPaddingLeft() + mInsetStart;
      right = parent.getWidth() - parent.getPaddingRight() - mInsetEnd;
      canvas.clipRect(left, parent.getPaddingTop(), right,
          parent.getHeight() - parent.getPaddingBottom());
    } else {
      left = mInsetStart;
      right = parent.getWidth() - mInsetEnd;
    }

    final int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      parent.getDecoratedBoundsWithMargins(child, mBounds);
      final int bottom = mBounds.bottom + Math.round(child.getTranslationY());
      final int top = bottom - mDivider.getIntrinsicHeight();
      mDivider.setBounds(left, top, right, bottom);
      mDivider.draw(canvas);
    }
    canvas.restore();
  }

  private void drawHorizontal(Canvas canvas, RecyclerView parent) {
    canvas.save();
    final int top;
    final int bottom;

    if (parent.getClipToPadding()) {
      top = parent.getPaddingTop() + mInsetStart;
      bottom = parent.getHeight() - parent.getPaddingBottom() - mInsetEnd;
      canvas.clipRect(parent.getPaddingLeft(), top,
          parent.getWidth() - parent.getPaddingRight(), bottom);
    } else {
      top = mInsetStart;
      bottom = parent.getHeight() - mInsetEnd;
    }

    final int childCount = parent.getChildCount();
    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      parent.getLayoutManager().getDecoratedBoundsWithMargins(child, mBounds);
      final int right = mBounds.right + Math.round(child.getTranslationX());
      final int left = right - mDivider.getIntrinsicWidth();
      mDivider.setBounds(left, top, right, bottom);
      mDivider.draw(canvas);
    }
    canvas.restore();
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                             RecyclerView.State state) {
    if (mDivider == null) {
      outRect.set(0, 0, 0, 0);
      return;
    }
    if (mOrientation == VERTICAL) {
      outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
    } else {
      outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
    }
  }
}
