package com.kickstarter.ui.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
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
import butterknife.InjectView;

public class CommentListViewHolder extends RecyclerView.ViewHolder {
  protected Comment comment;
  protected View view;
  protected Presenter presenter;

  public @InjectView(R.id.avatar) ImageView avatar;
  public @InjectView(R.id.creator_label) TextView creatorLabel;
  public @InjectView(R.id.user_label) TextView userLabelTextView;
  public @InjectView(R.id.name) TextView name;
  public @InjectView(R.id.post_date) TextView postDate;
  public @InjectView(R.id.comment_body) TextView commentBody;
  @Inject CurrentUser currentUser;  //check if backed project

  public CommentListViewHolder(final View view, final CommentFeedPresenter presenter) {
    super(view);
    this.view = view;
    this.presenter = presenter;

    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.inject(this, view);
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
      .into(avatar);
    name.setText(comment.author().name());
    postDate.setText(DateTimeUtils.relativeDateInWords(comment.createdAt(), false, true));
    commentBody.setText(comment.body());
  }
}
