package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Paginator;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresPresenter(ActivityFeedPresenter.class)
public final class ActivityFeedActivity extends BaseActivity<ActivityFeedPresenter> {
  private ActivityFeedAdapter adapter;
  public @Bind(R.id.recycler_view) RecyclerView recyclerView;
  private Paginator paginator;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feed_layout);
    ButterKnife.bind(this);

    adapter = new ActivityFeedAdapter(presenter);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    paginator = new Paginator(recyclerView, presenter.inputs::nextPage);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    paginator.stop();
  }

  public void showActivities(@NonNull final List<Activity> activities) {
    adapter.takeActivities(activities);
  }

  public void showEmptyFeed(@Nullable final User user) {
    adapter.takeEmptyFeed(user);
  }

  public void activityFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true)
      .putExtra(getString(R.string.intent_login_type), LoginToutActivity.REASON_GENERIC);
    startActivityForResult(intent, ActivityRequestCodes.ACTIVITY_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void discoverProjectsButtonOnClick() {
    final Intent intent = new Intent(this, DiscoveryActivity.class);
    startActivity(intent);
  }

  public void showProjectUpdate(@NonNull final Activity activity) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), activity.projectUpdateUrl());
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startProjectActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.ACTIVITY_FEED_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
  }
}
