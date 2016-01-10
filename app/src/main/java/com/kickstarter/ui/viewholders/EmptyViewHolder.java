package com.kickstarter.ui.viewholders;

import android.support.annotation.Nullable;
import android.view.View;

public final class EmptyViewHolder extends KSViewHolder {
  public EmptyViewHolder(final View view) {
    super(view);
  }

  @Override
  public boolean bindData(final @Nullable Object data) {
    return true;
  }

  @Override
  public void onBind() {
  }
}
