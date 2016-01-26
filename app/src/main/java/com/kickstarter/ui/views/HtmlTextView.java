package com.kickstarter.ui.views;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

public final class HtmlTextView extends TextView {
  public HtmlTextView(final @NonNull Context context) {
    super(context);
    init();
  }

  public HtmlTextView(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init(){
    setText(Html.fromHtml(getText().toString()));
  }
}
