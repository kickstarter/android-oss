package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.ui.adapters.ManageProjectNotificationsAdapter;
import com.kickstarter.viewmodels.ManageProjectNotificationsViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ManageProjectNotificationsViewModel.class)
public final class ManageProjectNotificationActivity extends BaseActivity<ManageProjectNotificationsViewModel>
  implements ManageProjectNotificationsAdapter.Delegate {
  private ManageProjectNotificationsAdapter adapter;

  protected @Bind(R.id.project_notifications_recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.manage_project_notifications_layout);
    ButterKnife.bind(this);

    adapter = new ManageProjectNotificationsAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(adapter::takeProjects);
  }
}