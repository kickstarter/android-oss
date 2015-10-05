package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;

public class EmptyViewHolder extends KsrViewHolder {
  public EmptyViewHolder(final View view) {
    super(view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
  }
}
