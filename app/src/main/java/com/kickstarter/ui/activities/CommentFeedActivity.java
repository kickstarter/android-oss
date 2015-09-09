package com.kickstarter.ui.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentsAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;

@RequiresPresenter(CommentFeedPresenter.class)
public class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> {
  @Nullable @Bind(R.id.comment_button) TextView commentButtonTextView;
  @Nullable @Bind(R.id.comment_feed_recycler_view) RecyclerView recyclerView;
  @Nullable @Bind(R.id.context_photo) ImageView projectPhotoImageView;
  @Nullable @Bind(R.id.project_name) TextView projectNameTextView;
  @Nullable @Bind(R.id.creator_name) TextView creatorNameTextView;
  private Project project;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Intent intent = getIntent();
    project = intent.getParcelableExtra(getString(R.string.intent_project));
    final int layout = (project.commentsCount == 0) ? R.layout.empty_comment_feed_layout : R.layout.comment_feed_layout;
    setContentView(layout);
    ButterKnife.bind(this);

    // messy WIP
//    commentButtonTextView.setVisibility(project.isBacking() ? View.VISIBLE : View.GONE);  // move this to toolbar activity
    if (project.commentsCount != 0) {
      presenter.takeProject(project);
    }
    else {
      showProjectContext(project);
    }
  }

  public void showProjectContext(Project project) {
    Picasso.with(getApplicationContext()).load(project.photo().full())
      .into(projectPhotoImageView);
    projectNameTextView.setText(project.name());
    creatorNameTextView.setText(project.creator().name());
  }

  public void showComments(final List<Comment> comments) {
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    final CommentsAdapter adapter = new CommentsAdapter(comments, project, presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  @Override
  @Nullable @OnClick(R.id.nav_back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void projectContextClick(View view) {
    onBackPressed();
  }

  public void publicCommentClick(View view) {
    final Dialog dialog = new Dialog(view.getContext());
    dialog.setTitle("Post Public Comment");
    dialog.show();
  }
}
