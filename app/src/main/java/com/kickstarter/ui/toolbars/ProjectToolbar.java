package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.util.AttributeSet;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectToolbar extends KSToolbar {
  public ProjectToolbar(final @NonNull Context context) {
    super(context);
  }

  public ProjectToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public ProjectToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.back_icon)
  public void backIconClick() {
    ((BaseActivity) getContext()).back();
  }
}
