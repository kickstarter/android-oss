package com.kickstarter.ui.views;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import butterknife.ButterKnife;

public class CommentFeedToolbar extends Toolbar {
  // set project observable here
  Context context;

  public CommentFeedToolbar(final Context context) {
    super(context);
  }

  public CommentFeedToolbar(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public CommentFeedToolbar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
    context = getContext();
  }
}
