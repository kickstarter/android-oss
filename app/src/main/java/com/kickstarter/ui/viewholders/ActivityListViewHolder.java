package com.kickstarter.ui.viewholders;

import android.view.View;

import com.kickstarter.models.Activity;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public abstract class ActivityListViewHolder extends KSViewHolder {
  private Activity activity;

  public ActivityListViewHolder(final @NonNull View view) {
    super(view);
  }

  @CallSuper @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.activity = requireNonNull((Activity) data, Activity.class);
  }

  protected @NonNull Activity activity() {
    return this.activity;
  }
}
