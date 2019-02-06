package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.util.Pair;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.ui.adapters.ProjectNotificationSettingsAdapter;
import com.kickstarter.viewmodels.ProjectNotificationSettingsViewModel;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.extensions.ActivityExtKt.showSnackbar;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(ProjectNotificationSettingsViewModel.ViewModel.class)
public final class ProjectNotificationSettingsActivity extends BaseActivity<ProjectNotificationSettingsViewModel.ViewModel> {
  protected @Bind(R.id.project_notification_settings_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.general_error_something_wrong) String generalErrorString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_notification_settings_layout);
    ButterKnife.bind(this);

    final ProjectNotificationSettingsAdapter adapter = new ProjectNotificationSettingsAdapter();
    this.recyclerView.setAdapter(adapter);
    this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewModel.outputs.projectNotifications()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::projectNotifications);

    this.viewModel.outputs.unableToFetchProjectNotificationsError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> showSnackbar(this.recyclerView, this.generalErrorString));
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
