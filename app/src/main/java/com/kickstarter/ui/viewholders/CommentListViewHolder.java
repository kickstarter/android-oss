package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.libs.CommentUtils;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Bind;

public class CommentListViewHolder extends RecyclerView.ViewHolder {
  protected Comment comment;
  protected View view;
  protected Presenter presenter;

  public @Bind(R.id.avatar) ImageView avatarImageView;
  public @Bind(R.id.creator_label) TextView creatorLabel;
  public @Bind(R.id.user_label) TextView userLabelTextView;
  public @Bind(R.id.name) TextView name;
  public @Bind(R.id.post_date) TextView postDate;
  public @Bind(R.id.comment_body) TextView commentBody;
  @Inject CurrentUser currentUser;  //check if backed project

  public CommentListViewHolder(final View view, final CommentFeedPresenter presenter) {
    super(view);
    this.view = view;
    this.presenter = presenter;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final Comment comment, final Project project) {
    this.comment = comment;

    if (CommentUtils.isUserAuthor(comment, project.creator())) {
      creatorLabel.setVisibility(View.VISIBLE);
    }
    else if (CommentUtils.isUserAuthor(comment, currentUser.getUser())) {
      userLabelTextView.setVisibility(View.VISIBLE);
    }
    else {
      creatorLabel.setVisibility(View.GONE);
      userLabelTextView.setVisibility(View.GONE);
    }

    Picasso.with(view.getContext()).load(comment.author()
      .avatar()
      .small())
      .transform(new CircleTransform())
      .into(avatarImageView);
    name.setText(comment.author().name());
    postDate.setText(DateTimeUtils.relativeDateInWords(comment.createdAt(), false, true));
    commentBody.setText(comment.body());
  }
}
