package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class EmptyActivityFeedViewHolder extends KSViewHolder {
  protected @Bind(R.id.discover_projects_button) Button discoverProjectsButton;
  protected @Bind(R.id.login_button) Button loginButton;

  private final Delegate delegate;

  public interface Delegate {
    void emptyActivityFeedDiscoverProjectsClicked(EmptyActivityFeedViewHolder viewHolder);
    void emptyActivityFeedLoginClicked(EmptyActivityFeedViewHolder viewHolder);
  }

  public EmptyActivityFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(@Nullable Object user) {
    if (user == null) {
      discoverProjectsButton.setVisibility(View.GONE);
      loginButton.setVisibility(View.VISIBLE);
    } else  {
      discoverProjectsButton.setVisibility(View.VISIBLE);
      loginButton.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.discover_projects_button)
  public void discoverProjectsOnClick() {
    delegate.emptyActivityFeedDiscoverProjectsClicked(this);
  }

  @OnClick(R.id.login_button)
  public void loginOnClick() {
    delegate.emptyActivityFeedLoginClicked(this);
  }
}
