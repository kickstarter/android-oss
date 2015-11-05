package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import butterknife.ButterKnife;

public class CommentFeedToolbar extends KSToolbar {
  public CommentFeedToolbar(@NonNull final Context context) {
    super(context);
  }

  public CommentFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public CommentFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
