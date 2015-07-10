package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.libs.Font;

import javax.inject.Inject;

public class TiemposTextView extends TextView {
  @Inject Font font;

  public TiemposTextView(final Context context) {
    this(context, null);
  }

  public TiemposTextView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TiemposTextView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public TiemposTextView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);
    setTypeface(font.tiemposTypeface());
  }
}
