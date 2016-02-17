package com.kickstarter.ui.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.models.Activity;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public abstract class ActivityListViewHolder extends KSViewHolder {
  private Activity activity;

  public ActivityListViewHolder(final @NonNull View view) {
    super(view);
  }

  @CallSuper @Override
  public void bindData(final @Nullable Object data) throws Exception {
    activity = requireNonNull((Activity) data, Activity.class);
  }

  protected @NonNull Activity activity() {
    return activity;
  }
}
