package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class EmptyCommentFeedViewHolder extends KSViewHolder {
  private final Delegate delegate;
  protected @Bind(R.id.comment_feed_login_button) Button commentFeedLoginButton;
  protected @Bind(R.id.no_comments_message) TextView noCommentsMessageTextView;

  public interface Delegate {
    void emptyCommentFeedLoginClicked(EmptyCommentFeedViewHolder viewHolder);
  }

  public EmptyCommentFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    final Pair<Project, User> projectAndUser = (Pair<Project, User>) datum;
    final Project project = projectAndUser.first;
    final User user = projectAndUser.second;

    if (user == null) {
      commentFeedLoginButton.setVisibility(View.VISIBLE);
      noCommentsMessageTextView.setText(R.string.___Aw_how_sad_Log_in);
    } else if (project.isBacking()) {
      commentFeedLoginButton.setVisibility(View.GONE);
      noCommentsMessageTextView.setText(R.string.project_comments_empty_state_backer_message);
    } else {
      commentFeedLoginButton.setVisibility(View.GONE);
      noCommentsMessageTextView.setText(R.string.update_comments_empty_state_non_backer_message);
    }
  }

  @Nullable
  @OnClick(R.id.comment_feed_login_button)
  public void emptyCommentFeedLogin() {
    delegate.emptyCommentFeedLoginClicked(this);
  }
}
