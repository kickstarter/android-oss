package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.SwipeRefresher;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ApplicationUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ActivityFeedAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.ActivityFeedViewModel;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(ActivityFeedViewModel.ViewModel.class)
public final class ActivityFeedActivity extends BaseActivity<ActivityFeedViewModel.ViewModel> {
  private ActivityFeedAdapter adapter;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.activity_feed_swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;

  @Inject CurrentUserType currentUser;

  private RecyclerViewPaginator recyclerViewPaginator;
  private SwipeRefresher swipeRefresher;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((KSApplication) getApplication()).component().inject(this);
    setContentView(R.layout.activity_feed_layout);
    ButterKnife.bind(this);

    adapter = new ActivityFeedAdapter(viewModel.inputs);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    recyclerViewPaginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);
    swipeRefresher = new SwipeRefresher(this, swipeRefreshLayout, viewModel.inputs::refresh, viewModel.outputs::isFetchingActivities);

    // Only allow refreshing if there's a current user
    currentUser.observable()
      .map(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(swipeRefreshLayout::setEnabled);

    viewModel.outputs.activities()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showActivities);

    viewModel.outputs.goToDiscovery()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> resumeDiscoveryActivity());

    viewModel.outputs.goToLogin()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> startActivityFeedLogin());

    viewModel.outputs.goToProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);

    viewModel.outputs.goToProjectUpdate()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectUpdateActivity);

    viewModel.outputs.goToSurvey()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startSurveyWebView);

    viewModel.outputs.loggedOutEmptyStateIsVisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::showLoggedOutEmptyState);

    viewModel.outputs.loggedInEmptyStateIsVisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(adapter::showLoggedInEmptyState);

    viewModel.outputs.surveys()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showSurveys);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerViewPaginator.stop();
    recyclerView.setAdapter(null);
  }

  private void showActivities(final @NonNull List<Activity> activities) {
    adapter.takeActivities(activities);
  }

  private void showSurveys(final @NonNull List<SurveyResponse> surveyResponses) {
    adapter.takeSurveys(surveyResponses);
  }

  private void resumeDiscoveryActivity() {
    ApplicationUtils.resumeDiscoveryActivity(this);
  }

  private void startActivityFeedLogin() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.ACTIVITY_FEED);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
  }

  private void startProjectActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, RefTag.activity());
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProjectUpdateActivity(final @NonNull Activity activity) {
    final Intent intent = new Intent(this, WebViewActivity.class)
      .putExtra(IntentKey.URL, activity.projectUpdateUrl());
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startSurveyWebView(final @NonNull SurveyResponse surveyResponse) {
    final Intent intent = new Intent(this, WebViewActivity.class)
      .putExtra(IntentKey.URL, surveyResponse.urlsEnvelope().webEnvelope().survey());
    startActivityWithTransition(intent, R.anim.slide_in_bottom, R.anim.com_mixpanel_android_slide_down);
  }
}
