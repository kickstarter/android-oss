package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;

public final class EmptyViewHolder extends KSViewHolder {
  public EmptyViewHolder(final View view) {
    super(view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
  }
}
