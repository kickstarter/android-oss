package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.kickstarter.R;

public class DebugView extends FrameLayout {
  public DebugView(final Context context) {
    this(context, null);
  }

  public DebugView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DebugView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    LayoutInflater.from(context).inflate(R.layout.debug_view, this);
  }
}
