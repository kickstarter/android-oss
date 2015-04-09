package com.kickstarter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.libs.Font;
import com.kickstarter.KsrApplication;

import javax.inject.Inject;

public class IonIconTextView extends TextView {
  @Inject Font font;

  public IonIconTextView(Context context) {
    super(context);
  }

  public IonIconTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public IonIconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);
    setTypeface(font.ionIconTypeface());
  }
}
