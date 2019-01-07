package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.CommentUtils;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CommentViewHolder extends KSViewHolder {
  private Comment comment;
  private final CurrentUserType currentUser;
  private final KSString ksString;
  private Project project;

  public @Bind(R.id.avatar) ImageView avatarImageView;
  public @Bind(R.id.creator_label) TextView creatorLabelTextView;
  public @Bind(R.id.user_label) TextView userLabelTextView;
  public @Bind(R.id.name) TextView nameTextView;
  public @Bind(R.id.post_date) TextView postDateTextView;
  public @Bind(R.id.comment_body) TextView commentBodyTextView;

  public @BindColor(R.color.text_secondary) int textSecondaryColor;
  public @BindColor(R.color.text_primary) int textPrimaryColor;

  public CommentViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.currentUser = environment().currentUser();
    this.ksString = environment().ksString();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, Comment> projectAndComment = requireNonNull((Pair<Project, Comment>) data);
    this.project = requireNonNull(projectAndComment.first, Project.class);
    this.comment = requireNonNull(projectAndComment.second, Comment.class);
  }

  public void onBind() {
    final Context context = context();

    this.creatorLabelTextView.setVisibility(View.GONE);
    this.userLabelTextView.setVisibility(View.GONE);

    if (CommentUtils.isUserAuthor(this.comment, this.project.creator())) {
      this.creatorLabelTextView.setVisibility(View.VISIBLE);
    } else if (CommentUtils.isUserAuthor(this.comment, this.currentUser.getUser())) {
      this.userLabelTextView.setVisibility(View.VISIBLE);
    }

    Picasso.with(context).load(this.comment.author()
      .avatar()
      .small())
      .transform(new CircleTransformation())
      .into(this.avatarImageView);

    this.nameTextView.setText(this.comment.author().name());
    this.postDateTextView.setText(DateTimeUtils.relative(context, this.ksString, this.comment.createdAt()));

    if (CommentUtils.isDeleted(this.comment)) {
      this.commentBodyTextView.setTextColor(this.textSecondaryColor);
      this.commentBodyTextView.setTypeface(this.commentBodyTextView.getTypeface(), Typeface.ITALIC);
    } else {
      this.commentBodyTextView.setTextColor(this.textPrimaryColor);
      this.commentBodyTextView.setTypeface(this.commentBodyTextView.getTypeface(), Typeface.NORMAL);
    }
    this.commentBodyTextView.setText(this.comment.body());
  }
}
