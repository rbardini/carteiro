package com.rbardini.carteiro.ui.swipedismiss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.rbardini.carteiro.util.UIUtils;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeableRecyclerView extends RecyclerView {
  public interface RecyclerViewSwipeListener {
    void onSwipe(int position);
  }

  public interface SwipeableViewHolder {}

  final private AdapterDataObserver observer = new AdapterDataObserver() {
    @Override
    public void onChanged() {
      checkIfEmpty();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      checkIfEmpty();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      checkIfEmpty();
    }
  };

  private int mSwipeDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
  private Drawable mLeaveBehindBackground = new ColorDrawable(Color.TRANSPARENT);
  private int mLeaveBehindPadding = 0;
  private Bitmap mLeaveBehindIcon;
  private View mEmptyView;

  public SwipeableRecyclerView(Context context) {
    super(context);
  }

  public SwipeableRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public SwipeableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void setAdapter(Adapter adapter) {
    final Adapter oldAdapter = getAdapter();

    if (oldAdapter != null) {
      oldAdapter.unregisterAdapterDataObserver(observer);
    }

    super.setAdapter(adapter);

    if (adapter != null) {
      adapter.registerAdapterDataObserver(observer);
    }

    checkIfEmpty();
  }

  public void setSwipeDirs(int swipeDirs) {
    mSwipeDirs = swipeDirs;
  }

  public void setLeaveBehindColor(@ColorInt int color) {
    mLeaveBehindBackground = new ColorDrawable(color);
  }

  public void setLeaveBehindPadding(@Dimension int padding) {
    mLeaveBehindPadding = padding;
  }

  public void setLeaveBehindIcon(@DrawableRes int drawableId) {
    mLeaveBehindIcon = UIUtils.getBitmapFromDrawable(getContext(), drawableId);
  }

  public void setEmptyView(View emptyView) {
    this.mEmptyView = emptyView;
    checkIfEmpty();
  }

  private void checkIfEmpty() {
    if (mEmptyView != null && getAdapter() != null) {
      final boolean isVisible = getAdapter().getItemCount() == 0;

      mEmptyView.setVisibility(isVisible ? VISIBLE : GONE);
      setVisibility(isVisible ? GONE : VISIBLE);
    }
  }

  public void setSwipeListener(final RecyclerViewSwipeListener listener) {
    setUpItemTouchHelper(listener);
    setUpItemDecoration();
  }

  private void setUpItemTouchHelper(final RecyclerViewSwipeListener listener) {
    new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, mSwipeDirs) {
      private final Paint paint = new Paint();

      @Override
      public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
        return false;
      }

      @Override
      public void onSwiped(ViewHolder viewHolder, int direction) {
        listener.onSwipe(viewHolder.getAdapterPosition());
      }

      @Override
      public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return;

        if (!(viewHolder instanceof SwipeableViewHolder) || viewHolder.getAdapterPosition() == -1) return;

        final int deltaX = (int) dX;
        if (deltaX == 0) return;

        final View itemView = viewHolder.itemView;
        final Rect leaveBounds = new Rect();
        final boolean toLeft = deltaX > 0;

        leaveBounds.set(toLeft ? itemView.getLeft() : itemView.getRight() + deltaX, itemView.getTop(),
            toLeft ? itemView.getLeft() + deltaX : itemView.getRight(), itemView.getBottom());

        mLeaveBehindBackground.setBounds(leaveBounds);
        mLeaveBehindBackground.draw(c);

        if (mLeaveBehindIcon != null && leaveBounds.width() > mLeaveBehindPadding) {
          final Bitmap clippedIcon;

          if (leaveBounds.width() >= mLeaveBehindPadding + mLeaveBehindIcon.getWidth()) {
            clippedIcon = mLeaveBehindIcon;

          } else {
            int clippedIconWidth = leaveBounds.width() - mLeaveBehindPadding;
            clippedIcon = Bitmap.createBitmap(mLeaveBehindIcon, toLeft ? 0 : mLeaveBehindIcon.getWidth() - clippedIconWidth,
                0, clippedIconWidth, mLeaveBehindIcon.getHeight());
          }

          c.drawBitmap(clippedIcon, toLeft ? leaveBounds.left + mLeaveBehindPadding : leaveBounds.right - clippedIcon.getWidth() - mLeaveBehindPadding,
              (leaveBounds.top + leaveBounds.bottom - clippedIcon.getHeight()) / 2, paint);
        }
      }
    }).attachToRecyclerView(this);
  }

  private void setUpItemDecoration() {
    addItemDecoration(new ItemDecoration() {
      @Override
      public void onDraw(Canvas c, RecyclerView parent, State state) {
        LayoutManager layoutManager = getLayoutManager();

        if (parent.getItemAnimator().isRunning()) {
          int childCount = layoutManager.getChildCount();
          View lastViewComingDown = null;
          View firstViewComingUp = null;
          boolean isAdding = false;

          for (int i = 0; i < childCount; i++) {
            View child = layoutManager.getChildAt(i);
            float dY = child.getTranslationY();

            // There is no easy way to check whether the animator is animating items in or out,
            // so check if any child view is not fully opaque instead
            if (child.getAlpha() < 1f) {
              isAdding = true;
              break;
            }

            if (dY < 0) {
              lastViewComingDown = child;

            } else if (dY > 0) {
              if (firstViewComingUp == null) {
                firstViewComingUp = child;
              }
            }
          }

          if (!isAdding) {
            int left = 0, right = parent.getWidth(), top = 0, bottom = 0;

            if (lastViewComingDown != null && firstViewComingUp != null) {
              top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
              bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();

            } else if (lastViewComingDown != null) {
              top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
              bottom = lastViewComingDown.getBottom();

            } else if (firstViewComingUp != null) {
              top = firstViewComingUp.getTop();
              bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
            }

            mLeaveBehindBackground.setBounds(left, top, right, bottom);
            mLeaveBehindBackground.draw(c);
          }
        }

        super.onDraw(c, parent, state);
      }
    });
  }
}
