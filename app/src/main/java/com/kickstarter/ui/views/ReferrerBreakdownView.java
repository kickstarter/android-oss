package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.kickstarter.libs.ReferrerColor;

public class ReferrerBreakdownView extends View {

  public Canvas canvas;
  private Double customSweepAngle;
  private Double externalSweepAngle;
  private Double internalSweepAngle;
  private RectF outerRectangle;
  private RectF innerRectangle;
  private Paint paint;
  private static final int innerCircleOffset = 30;

  public ReferrerBreakdownView(final Context context, final AttributeSet attributeSet) {
    super(context, attributeSet);
    this.paint = new Paint();
    this.outerRectangle = new RectF(0, 0, 0, 0);
    this.innerRectangle = new RectF(0, 0, 0, 0);
  }

  public void setCustomAngleAndColor(final @NonNull Double sweepAngle) {
    this.customSweepAngle = sweepAngle;
  }

  public void setExternalAngleAndColor(final @NonNull Double sweepAngle) {
    this.externalSweepAngle = sweepAngle;
  }

  public void setInternalAngleAndColor(final @NonNull Double sweepAngle) {
    this.internalSweepAngle = sweepAngle;
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    super.onDraw(canvas);
    this.canvas = canvas;

    this.paint.setAntiAlias(true);
    this.paint.setStyle(Paint.Style.FILL);

    final float bottom = getHeight();
    final float left = getX();
    final float right = getWidth();
    final float top = getY();

    this.outerRectangle.set(left, top, right, bottom);
    this.innerRectangle.set(left + innerCircleOffset, top + innerCircleOffset, right - innerCircleOffset, bottom - innerCircleOffset);

    Float offset = 0f;

    this.paint.setColor(ReferrerColor.CUSTOM.getReferrerColor());
    this.canvas.drawArc(this.outerRectangle, offset, this.customSweepAngle.floatValue(), true, this.paint);
    offset = offset + this.customSweepAngle.floatValue();

    this.paint.setColor(ReferrerColor.EXTERNAL.getReferrerColor());
    this.canvas.drawArc(this.outerRectangle, offset, this.externalSweepAngle.floatValue(), true, this.paint);
    offset = offset + this.externalSweepAngle.floatValue();

    this.paint.setColor(ReferrerColor.INTERNAL.getReferrerColor());
    this.canvas.drawArc(this.outerRectangle, offset, this.internalSweepAngle.floatValue(), true, this.paint);

    this.paint.setColor(Color.WHITE);
    this.canvas.drawArc(this.innerRectangle, 0, 360f, true, this.paint);
  }
}
