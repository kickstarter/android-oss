package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.adapters.ProjectNotificationSettingsAdapter;
import com.kickstarter.viewmodels.ProjectNotificationSettingsViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresViewModel(ProjectNotificationSettingsViewModel.class)
public final class ProjectNotificationSettingsActivity extends BaseActivity<ProjectNotificationSettingsViewModel> {
  protected @Bind(R.id.project_notification_settings_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.general_error_something_wrong) String generalErrorString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_notification_settings_layout);
    ButterKnife.bind(this);

    final ProjectNotificationSettingsAdapter adapter = new ProjectNotificationSettingsAdapter();
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    viewModel.outputs.projectNotifications()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::projectNotifications);

    viewModel.errors.unableToFetchProjectNotificationsError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .map(__ -> generalErrorString)
      .subscribe(ViewUtils.showToast(this));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    recyclerView.setAdapter(null);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
