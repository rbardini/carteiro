package com.rbardini.carteiro.ui.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.Property;

import com.rbardini.carteiro.util.AnimUtils;

/**
 * A drawable that can morph size, shape (via it's corner radius) and color.  Specifically this is
 * useful for animating between a FAB and a dialog.
 */
public class MorphDrawable extends Drawable {
  public static final Property CORNER_RADIUS = AnimUtils.createFloatProperty(
      new AnimUtils.FloatProp<MorphDrawable>("cornerRadius") {
        @Override
        public void set(MorphDrawable morphDrawable, float value) {
          morphDrawable.setCornerRadius(value);
        }

        @Override
        public float get(MorphDrawable morphDrawable) {
          return morphDrawable.getCornerRadius();
        }
      });

  public static final Property<MorphDrawable, Integer> COLOR = AnimUtils.createIntProperty(
      new AnimUtils.IntProp<MorphDrawable>("color") {
        @Override
        public void set(MorphDrawable morphDrawable, int color) {
          morphDrawable.setColor(color);
        }

        @Override
        public int get(MorphDrawable morphDrawable) {
          return morphDrawable.getColor();
        }
      });

  private Paint paint;
  private float cornerRadius;

  public MorphDrawable(@ColorInt int color, float cornerRadius) {
    this.cornerRadius = cornerRadius;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(color);
  }

  float getCornerRadius() {
    return cornerRadius;
  }

  void setCornerRadius(float cornerRadius) {
    this.cornerRadius = cornerRadius;
    invalidateSelf();
  }

  public int getColor() {
    return paint.getColor();
  }

  public void setColor(@ColorInt int color) {
    paint.setColor(color);
    invalidateSelf();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    canvas.drawRoundRect(
        getBounds().left,
        getBounds().top,
        getBounds().right,
        getBounds().bottom,
        cornerRadius,
        cornerRadius,
        paint);
  }

  @Override
  public void getOutline(@NonNull Outline outline) {
    outline.setRoundRect(getBounds(), cornerRadius);
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    paint.setColorFilter(cf);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return paint.getAlpha() == 255 ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT;
  }
}
