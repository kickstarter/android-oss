package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

public class EmptyCommentFeedViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  protected @Bind(R.id.comment_feed_login_button) Button commentFeedLoginButton;
  protected @Bind(R.id.no_comments_message) TextView noCommentsMessageTextView;

  public interface Delegate {
    void emptyCommentFeedLoginClicked(@NonNull final EmptyCommentFeedViewHolder viewHolder);
  }

  public EmptyCommentFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(@Nullable final Object datum) {
    final Pair<Project, User> projectAndUser = (Pair<Project, User>) datum;
    final Project project = projectAndUser.first;
    final User user = projectAndUser.second;

    int loginButtonVisibility = View.GONE;
    @StringRes int messageStringId;

    if (user == null) {
      loginButtonVisibility = View.VISIBLE;
      messageStringId = R.string.Aw_how_sad_Log_in;
    } else if (project.isBacking()) {
      messageStringId = R.string.Aw_how_sad_Be_the_first;
    } else {
      messageStringId = R.string.Aw_how_sad_Become_a_backer;
    }
    commentFeedLoginButton.setVisibility(loginButtonVisibility);
    noCommentsMessageTextView.setText(messageStringId);
  }

  @Nullable
  @OnClick(R.id.comment_feed_login_button)
  public void emptyCommentFeedLogin() {
    delegate.emptyCommentFeedLoginClicked(this);
  }
}
