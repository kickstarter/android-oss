package com.kickstarter.ui.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.models.Activity;

public abstract class ActivityListViewHolder extends KSViewHolder {
  protected Activity activity;

  public ActivityListViewHolder(final @NonNull View view) {
    super(view);
  }

  @CallSuper @Override
  public boolean bindData(final @Nullable Object data) {
    try {
      activity = (Activity) data;
      return activity != null;
    } catch (Exception __) {
      return false;
    }
  }
}
