package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class HamburgerNavigationHeaderLoggedOutViewHolder extends KSViewHolder {
  public HamburgerNavigationHeaderLoggedOutViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
  }

  @Override
  public void onBind() {
  }

  @OnClick(R.id.logged_out_container)
  public void loggedOutClick() {
  }
}
