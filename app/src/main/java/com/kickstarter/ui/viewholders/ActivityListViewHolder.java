package com.kickstarter.ui.viewholders;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.models.Activity;

public abstract class ActivityListViewHolder extends KsrViewHolder {
  protected Activity activity;

  public ActivityListViewHolder(@NonNull final View view) {
    super(view);
  }

  @CallSuper
  public void onBind(@NonNull final Object datum) {
    this.activity = (Activity) datum;
  }
}
