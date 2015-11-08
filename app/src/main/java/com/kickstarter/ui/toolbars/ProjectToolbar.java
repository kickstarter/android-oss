package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.kickstarter.R;
import com.kickstarter.ui.activities.ProjectActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectToolbar extends KSToolbar {
  public ProjectToolbar(@NonNull final Context context) {
    super(context);
  }

  public ProjectToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public ProjectToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.back_icon)
  public void backIconClick() {
    ((ProjectActivity) getContext()).onBackPressed();
  }
}
