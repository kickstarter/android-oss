package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.CommentUtils;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CommentViewHolder extends KSViewHolder {
  private Project project;
  private Comment comment;
  public @Bind(R.id.avatar) ImageView avatarImageView;
  public @Bind(R.id.creator_label) TextView creatorLabelTextView;
  public @Bind(R.id.user_label) TextView userLabelTextView;
  public @Bind(R.id.name) TextView nameTextView;
  public @Bind(R.id.post_date) TextView postDateTextView;
  public @Bind(R.id.comment_body) TextView commentBodyTextView;

  protected @Inject CurrentUser currentUser;
  protected @Inject KSString ksString;

  public CommentViewHolder(final @NonNull View view) {
    super(view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, Comment> projectAndComment = requireNonNull((Pair<Project, Comment>) data);
    project = requireNonNull(projectAndComment.first, Project.class);
    comment = requireNonNull(projectAndComment.second, Comment.class);
  }

  public void onBind() {
    final Context context = context();

    creatorLabelTextView.setVisibility(View.GONE);
    userLabelTextView.setVisibility(View.GONE);

    if (CommentUtils.isUserAuthor(comment, project.creator())) {
      creatorLabelTextView.setVisibility(View.VISIBLE);
    } else if (CommentUtils.isUserAuthor(comment, currentUser.getUser())) {
      userLabelTextView.setVisibility(View.VISIBLE);
    }

    Picasso.with(context).load(comment.author()
      .avatar()
      .small())
      .transform(new CircleTransformation())
      .into(avatarImageView);
    nameTextView.setText(comment.author().name());
    postDateTextView.setText(DateTimeUtils.relative(context, ksString, comment.createdAt()));
    commentBodyTextView.setText(comment.body());
  }
}
