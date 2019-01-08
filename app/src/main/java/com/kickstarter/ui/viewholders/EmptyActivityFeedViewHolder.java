package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.Button;

import com.kickstarter.R;
import com.kickstarter.libs.utils.BooleanUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class EmptyActivityFeedViewHolder extends KSViewHolder {
  private boolean isLoggedIn;
  protected @Bind(R.id.discover_projects_button) Button discoverProjectsButton;
  protected @Bind(R.id.login_button) Button loginButton;

  private final @Nullable Delegate delegate;

  public interface Delegate {
    void emptyActivityFeedDiscoverProjectsClicked(EmptyActivityFeedViewHolder viewHolder);
    void emptyActivityFeedLoginClicked(EmptyActivityFeedViewHolder viewHolder);
  }

  public EmptyActivityFeedViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.isLoggedIn = BooleanUtils.isTrue((Boolean) data);
  }

  @Override
  public void onBind() {
    if (this.isLoggedIn) {
      this.discoverProjectsButton.setVisibility(View.VISIBLE);
      this.loginButton.setVisibility(View.GONE);
    } else  {
      this.discoverProjectsButton.setVisibility(View.GONE);
      this.loginButton.setVisibility(View.VISIBLE);
    }
  }

  @OnClick(R.id.discover_projects_button)
  public void discoverProjectsOnClick() {
    if (this.delegate != null) {
      this.delegate.emptyActivityFeedDiscoverProjectsClicked(this);
    }
  }

  @OnClick(R.id.login_button)
  public void loginOnClick() {
    if (this.delegate != null) {
      this.delegate.emptyActivityFeedLoginClicked(this);
    }
  }
}
