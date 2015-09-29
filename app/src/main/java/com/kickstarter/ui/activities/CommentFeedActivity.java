package com.kickstarter.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentFeedAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;

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
    final int layout = (project.commentsCount() == null || project.commentsCount() == 0) ? R.layout.empty_comment_feed_layout : R.layout.comment_feed_layout;
    setContentView(layout);
    ButterKnife.bind(this);

    if (project.commentsCount() != null) {
      presenter.takeProject(project);
    }
    else {
      showProjectContext(project);
    }
  }

  public void loadProjectComments(final Project project, final List<Comment> comments) {
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    final List<Pair<Project, Comment>> projectAndComments = Observable.from(comments)
      .map(comment -> Pair.create(project, comment))
      .toList().toBlocking().single();
    final CommentFeedAdapter adapter = new CommentFeedAdapter(project, projectAndComments, presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  @OnClick(R.id.nav_back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @Nullable @OnClick(R.id.leave_comment_button)
  public void publicCommentClick(final View view) {
    final LayoutInflater layoutInflater = getLayoutInflater();
    final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
    builder.setTitle(getString(R.string.Public_comment));
    builder.setView(layoutInflater.inflate(R.layout.comment_dialog, null))
      .setPositiveButton(getString(R.string.Post), (DialogInterface dialog, int which) -> {
      })
      .setNegativeButton(getString(R.string.Cancel), (DialogInterface dialog, int which) -> {
      });

    builder.show();
  }

  public void testActivity() {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), "https://www.kickstarter.com");
    startActivity(intent);
  }
}
