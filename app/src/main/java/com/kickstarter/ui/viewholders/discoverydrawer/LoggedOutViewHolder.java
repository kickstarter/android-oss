package com.kickstarter.ui.viewholders.discoverydrawer;

import android.view.View;

import com.kickstarter.R;
import com.kickstarter.ui.viewholders.KSViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class LoggedOutViewHolder extends KSViewHolder {
  private Delegate delegate;

  public interface Delegate {
    void loggedOutViewHolderActivityClick(final @NonNull LoggedOutViewHolder viewHolder);
    void loggedOutViewHolderInternalToolsClick(final @NonNull LoggedOutViewHolder viewHolder);
    void loggedOutViewHolderLoginToutClick(final @NonNull LoggedOutViewHolder viewHolder);
    void loggedOutViewHolderHelpClick(final @NonNull LoggedOutViewHolder viewHolder);
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
  @OnClick(R.id.drawer_activity)
  public void activityClick() {
    this.delegate.loggedOutViewHolderActivityClick(this);
  }

  @OnClick(R.id.drawer_help)
  public void helpClick() {
    this.delegate.loggedOutViewHolderHelpClick(this);
  }

  @Nullable @OnClick(R.id.internal_tools)
  public void internalToolsClick() {
    this.delegate.loggedOutViewHolderInternalToolsClick(this);
  }

  @OnClick(R.id.logged_out_text_view)
  public void loginToutClick() {
    this.delegate.loggedOutViewHolderLoginToutClick(this);
  }
}
