package com.kickstarter.ui.viewholders;

import android.support.annotation.Nullable;
import android.view.View;

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
