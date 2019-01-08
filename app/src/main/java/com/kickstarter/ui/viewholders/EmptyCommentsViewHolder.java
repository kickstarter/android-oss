package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class EmptyCommentsViewHolder extends KSViewHolder {
  private Project project;
  private User user;
  private final Delegate delegate;
  protected @Bind(R.id.comments_login_button) Button commentsLoginButton;
  protected @Bind(R.id.no_comments_message) TextView noCommentsMessageTextView;

  public interface Delegate {
    void emptyCommentsLoginClicked(EmptyCommentsViewHolder viewHolder);
  }

  public EmptyCommentsViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, User> projectAndUser = requireNonNull((Pair<Project, User>) data);
    this.project = requireNonNull(projectAndUser.first, Project.class);
    this.user = projectAndUser.second;
  }

  public void onBind() {
    if (this.user == null) {
      this.commentsLoginButton.setVisibility(View.VISIBLE);
      this.noCommentsMessageTextView.setText(R.string.project_comments_empty_state_logged_out_message_log_in);
    } else if (this.project.isBacking()) {
      this.commentsLoginButton.setVisibility(View.GONE);
      this.noCommentsMessageTextView.setText(R.string.project_comments_empty_state_backer_message);
    } else {
      this.commentsLoginButton.setVisibility(View.GONE);
      this.noCommentsMessageTextView.setText(R.string.update_comments_empty_state_non_backer_message);
    }
  }

  @Nullable
  @OnClick(R.id.comments_login_button)
  public void emptyCommentsLogin() {
    this.delegate.emptyCommentsLoginClicked(this);
  }
}
