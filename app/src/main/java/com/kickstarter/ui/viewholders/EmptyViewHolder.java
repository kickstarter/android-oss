package com.kickstarter.ui.viewholders;

import android.view.View;

import androidx.annotation.Nullable;

public final class EmptyViewHolder extends KSViewHolder {
  public EmptyViewHolder(final View view) {
    super(view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
  }

  @Override
  public void onBind() {
  }
}
