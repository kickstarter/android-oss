package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyCommentFeedViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  protected @Bind(R.id.comment_feed_login_button) Button commentFeedLoginButton;

  public interface Delegate {
    void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder);
  }

  public EmptyCommentFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(@Nullable final Object user) {
    final int visibility = (user == null) ? View.VISIBLE : View.GONE;
    commentFeedLoginButton.setVisibility(visibility);
  }

  @Nullable
  @OnClick(R.id.comment_feed_login_button)
  public void emptyCommentFeedLogin() {
    delegate.emptyCommentFeedLoginClicked(this);
  }
}
