package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

@RequiresPresenter(CommentFeedPresenter.class)
public class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> {
  @InjectView(R.id.comment_feed_recycler_view) RecyclerView recyclerView;
  private Project project;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    project = intent.getParcelableExtra(getString(R.string.intent_project));
    presenter.takeProject(project);
  }

  public void showComments(final List<Comment> comments) {
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    final CommentsAdapter adapter = new CommentsAdapter(comments, project, presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
  }

  @Override
  @OnClick(R.id.nav_back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void projectContextClick(View view) {
    onBackPressed();
  }
}
