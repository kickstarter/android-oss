package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.kickstarter.R;

import butterknife.BindString;
import butterknife.ButterKnife;

public final class ActivityFeedToolbar extends KSToolbar {
  @BindString(R.string.___Not_implemented_yet) String notImplementedYetString;

  public ActivityFeedToolbar(@NonNull final Context context) {
    super(context);
  }

  public ActivityFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public ActivityFeedToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }
}
