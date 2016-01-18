package com.kickstarter.ui.viewholders.discoverydrawer;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.ui.viewholders.KSViewHolder;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class LoggedOutViewHolder extends KSViewHolder {
  private Delegate delegate;

  public interface Delegate {
    void loggedOutViewHolderLoginClick(final @NonNull LoggedOutViewHolder viewHolder);
  }

  public LoggedOutViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    ButterKnife.bind(this, view);
    this.delegate = delegate;
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
  }

  @Override
  public void onBind() {
  }

  @OnClick(R.id.logged_out_container)
  public void loginClick() {
    delegate.loggedOutViewHolderLoginClick(this);
  }
}
