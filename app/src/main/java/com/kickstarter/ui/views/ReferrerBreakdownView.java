package com.kickstarter.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.kickstarter.libs.ReferrerColor;

public class ReferrerBreakdownView extends View {

  public Canvas canvas;
  private Double customSweepAngle;
  private Double externalSweepAngle;
  private Double internalSweepAngle;
  private Double unknownSweepAngle;
  private RectF rectF;

  public ReferrerBreakdownView(Context context) {
    super(context);
    setWillNotDraw(false);
  }

  public ReferrerBreakdownView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
  }

  public void setCustomAngleAndColor(Double sweepAngle) {
    this.customSweepAngle = sweepAngle;
  }

  public void setExternalAngleAndColor(Double sweepAngle) {
    this.externalSweepAngle = sweepAngle;
  }

  public void setInternalAngleAndColor(Double sweepAngle) {
    this.internalSweepAngle = sweepAngle;
  }

  public void setUnknownAngleAndColor(Double sweepAngle) {
    this.unknownSweepAngle = sweepAngle;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    this.canvas = canvas;

    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);

    float bottom = getHeight();
    float left = getX();
    float right = getWidth();
    float top = getY();

    this.rectF = new RectF(left, top, right, bottom);

    Float offset = 0f;

    paint.setColor(ReferrerColor.CUSTOM.getReferrerColor());
    this.canvas.drawArc(this.rectF, offset, this.customSweepAngle.floatValue(), true, paint);
    offset = offset + customSweepAngle.floatValue();

    paint.setColor(ReferrerColor.EXTERNAL.getReferrerColor());
    this.canvas.drawArc(this.rectF, offset, this.externalSweepAngle.floatValue(), true, paint);
    offset = offset + externalSweepAngle.floatValue();

    paint.setColor(ReferrerColor.INTERNAL.getReferrerColor());
    this.canvas.drawArc(this.rectF, offset, this.internalSweepAngle.floatValue(), true, paint);
    offset = offset + internalSweepAngle.floatValue();

    paint.setColor(ReferrerColor.CAMPAIGN.getReferrerColor());
    this.canvas.drawArc(this.rectF, offset, this.unknownSweepAngle.floatValue(), true, paint);
  }
}
