package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Notification;
import com.kickstarter.ui.adapters.ManageNotificationsAdapter;
import com.kickstarter.viewmodels.ManageNotificationsViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ManageNotificationsViewModel.class)
public final class ManageNotificationActivity extends BaseActivity<ManageNotificationsViewModel> {
  private ManageNotificationsAdapter adapter;

  protected @Bind(R.id.project_notifications_recycler_view) RecyclerView recyclerView;

  protected @BindString(R.string.___Unable_to_save) String unableToSaveString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.manage_notifications_layout);
    ButterKnife.bind(this);

    adapter = new ManageNotificationsAdapter(viewModel);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // repro this in the VH
    viewModel.outputs.projectNotification()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> {
        ViewUtils.showToast(this, "updated");
      });

    viewModel.errors.unableToSavePreferenceError()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> ViewUtils.showToast(this, unableToSaveString));
  }

  public void loadProjects(final @NonNull List<Notification> notifications) {
    adapter.takeProjects(notifications);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
