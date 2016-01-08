package com.kickstarter.ui.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.models.Activity;

public abstract class ActivityListViewHolder extends KSViewHolder {
  protected Activity activity;

  public ActivityListViewHolder(final @NonNull View view) {
    super(view);
  }

  @CallSuper
  public void onBind(final @NonNull Object datum) {
    this.activity = (Activity) datum;
  }
}
