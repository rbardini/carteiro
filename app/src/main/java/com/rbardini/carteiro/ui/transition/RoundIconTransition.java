package com.rbardini.carteiro.ui.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.AnimUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * A transition between a round icon & another surface using a circular reveal moving along an arc.
 * <p>
 * See: https://www.google.com/design/spec/motion/transforming-material.html#transforming-material-radial-transformation
 */
public class RoundIconTransition extends Transition {
  private static final String EXTRA_ROUND_ICON_COLOR = "EXTRA_ROUND_ICON_COLOR";
  private static final String EXTRA_ROUND_ICON_RES_ID = "EXTRA_ROUND_ICON_RES_ID";
  private static final String EXTRA_ROUND_ICON_SCALE_FACTOR = "EXTRA_ROUND_ICON_SCALE_FACTOR";
  private static final long DEFAULT_DURATION = 300L;
  private static final String PROP_BOUNDS = "carteiro:roundIconTransition:bounds";
  private static final String[] TRANSITION_PROPERTIES = {
    PROP_BOUNDS
  };

  private final int color;
  private final int icon;
  private final int scale;

  public RoundIconTransition(@ColorInt int iconColor, @DrawableRes int iconResId, int iconScaleFactor) {
    color = iconColor;
    icon = iconResId;
    scale = iconScaleFactor;
    setPathMotion(new GravityArcMotion());
    setDuration(DEFAULT_DURATION);
  }

  public RoundIconTransition(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = null;
    try {
      a = context.obtainStyledAttributes(attrs, R.styleable.RoundIconTransition);
      if (!a.hasValue(R.styleable.RoundIconTransition_color) || !a.hasValue(R.styleable.RoundIconTransition_icon)) {
        throw new IllegalArgumentException("Must provide both color & icon.");
      }
      color = a.getColor(R.styleable.RoundIconTransition_color, Color.TRANSPARENT);
      icon = a.getResourceId(R.styleable.RoundIconTransition_icon, 0);
      scale = a.getInt(R.styleable.RoundIconTransition_scale, 1);
      setPathMotion(new GravityArcMotion());
      if (getDuration() < 0) {
        setDuration(DEFAULT_DURATION);
      }
    } finally {
      a.recycle();
    }
  }

  /**
   * Configure {@code intent} with the extras needed to initialize this transition.
   */
  public static void addExtras(@NonNull Intent intent, @ColorInt int iconColor, @DrawableRes int iconResId, int iconScaleFactor) {
    intent.putExtra(EXTRA_ROUND_ICON_COLOR, iconColor);
    intent.putExtra(EXTRA_ROUND_ICON_RES_ID, iconResId);
    intent.putExtra(EXTRA_ROUND_ICON_SCALE_FACTOR, iconScaleFactor);
  }

  /**
   * Create a {@link RoundIconTransition} from the supplied {@code activity} extras and set as its
   * shared element enter/return transition.
   */
  public static boolean setup(@NonNull Activity activity, @Nullable View target) {
    final Intent intent = activity.getIntent();
    if (!intent.hasExtra(EXTRA_ROUND_ICON_COLOR) || !intent.hasExtra(EXTRA_ROUND_ICON_RES_ID)) {
      return false;
    }

    final int color = intent.getIntExtra(EXTRA_ROUND_ICON_COLOR, Color.TRANSPARENT);
    final int icon = intent.getIntExtra(EXTRA_ROUND_ICON_RES_ID, -1);
    final int scale = intent.getIntExtra(EXTRA_ROUND_ICON_SCALE_FACTOR, 1);
    final RoundIconTransition sharedEnter = new RoundIconTransition(color, icon, scale);
    if (target != null) {
      sharedEnter.addTarget(target);
    }
    activity.getWindow().setSharedElementEnterTransition(sharedEnter);
    return true;
  }

  @Override
  public String[] getTransitionProperties() {
    return TRANSITION_PROPERTIES;
  }

  @Override
  public void captureStartValues(TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  @Override
  public void captureEndValues(TransitionValues transitionValues) {
    captureValues(transitionValues);
  }

  @Override
  public Animator createAnimator(final ViewGroup sceneRoot, final TransitionValues startValues, final TransitionValues endValues) {
    if (startValues == null || endValues == null)  return null;

    final Rect startBounds = (Rect) startValues.values.get(PROP_BOUNDS);
    final Rect endBounds = (Rect) endValues.values.get(PROP_BOUNDS);

    final boolean fromRoundIcon = endBounds.width() > startBounds.width();
    final View view = endValues.view;
    final Rect dialogBounds = fromRoundIcon ? endBounds : startBounds;
    final Rect roundIconBounds = fromRoundIcon ? startBounds : endBounds;
    final Interpolator fastOutSlowInInterpolator = AnimUtils.getFastOutSlowInInterpolator(sceneRoot.getContext());
    final long duration = getDuration();
    final long halfDuration = duration / 2;
    final long twoThirdsDuration = duration * 2 / 3;

    if (!fromRoundIcon) {
      // Force measure / layout the dialog back to its original bounds
      view.measure(makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY),
          makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY));
      view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom);
    }

    final int translationX = startBounds.centerX() - endBounds.centerX();
    final int translationY = startBounds.centerY() - endBounds.centerY();
    if (fromRoundIcon) {
      view.setTranslationX(translationX);
      view.setTranslationY(translationY);
    }

    // Add a color overlay to fake appearance of the round icon
    final ColorDrawable roundIconColor = new ColorDrawable(color);
    roundIconColor.setBounds(0, 0, dialogBounds.width(), dialogBounds.height());
    if (!fromRoundIcon) roundIconColor.setAlpha(0);
    view.getOverlay().add(roundIconColor);

    // Add an icon overlay again to fake the appearance of the round icon
    final Drawable iconDrawable = ContextCompat.getDrawable(sceneRoot.getContext(), icon).mutate();
    final int iconWidth = iconDrawable.getIntrinsicWidth() / scale;
    final int iconHeight = iconDrawable.getIntrinsicHeight() / scale;
    final int iconLeft = (dialogBounds.width() - iconWidth) / 2;
    final int iconTop = (dialogBounds.height() - iconHeight) / 2;
    iconDrawable.setBounds(iconLeft, iconTop, iconLeft + iconWidth, iconTop + iconHeight);
    if (!fromRoundIcon) iconDrawable.setAlpha(0);
    view.getOverlay().add(iconDrawable);

    // Circular clip from/to the round icon size
    final Animator circularReveal;
    if (fromRoundIcon) {
      circularReveal = ViewAnimationUtils.createCircularReveal(view,
          view.getWidth() / 2,
          view.getHeight() / 2,
          startBounds.width() / 2,
          (float) Math.hypot(endBounds.width() / 2, endBounds.height() / 2));
      circularReveal.setInterpolator(AnimUtils.getFastOutLinearInInterpolator(sceneRoot.getContext()));
    } else {
      circularReveal = ViewAnimationUtils.createCircularReveal(view,
          view.getWidth() / 2,
          view.getHeight() / 2,
          (float) Math.hypot(startBounds.width() / 2, startBounds.height() / 2),
          endBounds.width() / 2);
      circularReveal.setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(sceneRoot.getContext()));

      // Persist the end clip i.e. stay at round icon size after the reveal has run
      circularReveal.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              final int left = (view.getWidth() - roundIconBounds.width()) / 2;
              final int top = (view.getHeight() - roundIconBounds.height()) / 2;
              outline.setOval(left, top, left + roundIconBounds.width(), top + roundIconBounds.height());
              view.setClipToOutline(true);
            }
          });
        }
      });
    }
    circularReveal.setDuration(duration);

    // Translate to end position along an arc
    final Animator translate = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, View.TRANSLATION_Y,
        fromRoundIcon ? getPathMotion().getPath(translationX, translationY, 0, 0)
                : getPathMotion().getPath(0, 0, -translationX, -translationY));
    translate.setDuration(duration);
    translate.setInterpolator(fastOutSlowInInterpolator);

    // Fade contents of non-round icon view in/out
    List<Animator> fadeContents = null;
    if (view instanceof ViewGroup) {
      final ViewGroup vg = ((ViewGroup) view);
      fadeContents = new ArrayList<>(vg.getChildCount());
      for (int i = vg.getChildCount() - 1; i >= 0; i--) {
        final View child = vg.getChildAt(i);
        final Animator fade = ObjectAnimator.ofFloat(child, View.ALPHA, fromRoundIcon ? 1f : 0f);
        if (fromRoundIcon) {
          child.setAlpha(0f);
        }
        fade.setDuration(twoThirdsDuration);
        fade.setInterpolator(fastOutSlowInInterpolator);
        fadeContents.add(fade);
      }
    }

    // Fade in/out the round icon color & icon overlays
    final Animator colorFade = ObjectAnimator.ofInt(roundIconColor, "alpha", fromRoundIcon ? 0 : 255);
    final Animator iconFade = ObjectAnimator.ofInt(iconDrawable, "alpha", fromRoundIcon ? 0 : 255);
    colorFade.setDuration(halfDuration);
    iconFade.setDuration(halfDuration);
    colorFade.setInterpolator(fastOutSlowInInterpolator);
    iconFade.setInterpolator(fastOutSlowInInterpolator);

    // Work around issue with elevation shadows. At the end of the return transition the shared
    // element's shadow is drawn twice (by each activity) which is jarring. This workaround
    // still causes the shadow to snap, but it's better than seeing it double drawn.
    Animator elevation = null;
    if (!fromRoundIcon) {
      elevation = ObjectAnimator.ofFloat(view, View.TRANSLATION_Z, -view.getElevation());
      elevation.setDuration(duration);
      elevation.setInterpolator(fastOutSlowInInterpolator);
    }

    // Run all animations together
    final AnimatorSet transition = new AnimatorSet();
    transition.playTogether(circularReveal, translate, colorFade, iconFade);
    transition.playTogether(fadeContents);
    if (elevation != null) transition.play(elevation);
    if (fromRoundIcon) {
      transition.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          // Clean up
          view.getOverlay().clear();
        }
      });
    }
    return new AnimUtils.NoPauseAnimator(transition);
  }

  private void captureValues(TransitionValues transitionValues) {
    final View view = transitionValues.view;
    if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) return;

    transitionValues.values.put(PROP_BOUNDS, new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
  }
}
