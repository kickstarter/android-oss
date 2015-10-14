package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(ActivityFeedPresenter.class)
public class ActivityFeedActivity extends BaseActivity<ActivityFeedPresenter> {
  ActivityFeedAdapter adapter;
  @Nullable @Bind(R.id.recycler_view) RecyclerView recyclerView;
  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ((KSApplication) getApplication()).component().inject(this);
    final int layout = currentUser.exists() ? R.layout.activity_feed_layout : R.layout.empty_activity_feed_layout;
    setContentView(layout);
    ButterKnife.bind(this);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  @Nullable @OnClick(R.id.discover_projects_button)
  public void discoverProjectsButtonOnClick() {
    final Intent intent = new Intent(this, DiscoveryActivity.class);
    startActivity(intent);
  }

  // todo: keep activity list position
  public void onItemsNext(@NonNull final List<Activity> activities) {
    adapter = new ActivityFeedAdapter(activities, presenter);
    recyclerView.setAdapter(adapter);
  }

  public void showProjectUpdate(@NonNull final Activity activity) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), activity.projectUpdateUrl());
    startActivity(intent);
  }

  public void startProjectActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
  }
}
