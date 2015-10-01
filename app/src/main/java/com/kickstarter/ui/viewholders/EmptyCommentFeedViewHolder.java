package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.models.Project;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyCommentFeedViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  private Project project;
  protected @Bind(R.id.comment_feed_login_button) Button commentFeedLoginButton;
  @Inject CurrentUser currentUser;

  public interface Delegate {
    void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder);
  }

  public EmptyCommentFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    project = (Project) datum;

    // show login button if logged out user
  }

  @Nullable
  @OnClick(R.id.comment_feed_login_button)
  public void emptyCommentFeedLogin() {
    delegate.emptyCommentFeedLoginClicked(this);
  }
}
