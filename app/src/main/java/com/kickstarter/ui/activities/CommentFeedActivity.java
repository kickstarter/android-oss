package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Comment;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.CommentFeedPresenter;
import com.kickstarter.ui.adapters.CommentListAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

@RequiresPresenter(CommentFeedPresenter.class)
public class CommentFeedActivity extends BaseActivity<CommentFeedPresenter> {
  @InjectView(R.id.project_background) ImageView projectBackground;
  @InjectView(R.id.project_name) TextView projectName;
  @InjectView(R.id.creator_name) TextView creatorName;

  CommentListAdapter adapter;
  LinearLayoutManager layoutManager;

  // todo: add subjects for pagination
  @InjectView(R.id.comment_feed_recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((KsrApplication) getApplication()).component().inject(this);
    setContentView(R.layout.comment_feed_layout);
    ButterKnife.inject(this);

    // todo: put this in a show() and hook up with subscription
    Intent intent = getIntent();
    Project project = intent.getExtras().getParcelable((getString(R.string.intent_project)));
    presenter.takeProjectComments(project); // yeah don't put this in the activity
    projectName.setText(project.name());
    creatorName.setText(project.creator().name());
    Picasso.with(getApplicationContext()).load(project.photo().full()).into(projectBackground);
  }

  public void show(final List<Comment> comments) {
    layoutManager = new LinearLayoutManager(this);
    adapter = new CommentListAdapter(comments, presenter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(adapter);
    recyclerView.setAdapter(adapter);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
