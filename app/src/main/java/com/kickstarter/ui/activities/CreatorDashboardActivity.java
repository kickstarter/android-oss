package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.viewmodels.CreatorDashboardViewModel;


import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel.class)
public final class CreatorDashboardActivity extends BaseActivity<CreatorDashboardViewModel.ViewModel> {

  protected @Bind(R.id.creator_dashboard_recycler_view) RecyclerView creatorDashboardRecyclerView;

  private CreatorDashboardAdapter adapter;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.creator_dashboard_layout);
    ButterKnife.bind(this);

    this.adapter = new CreatorDashboardAdapter();
    creatorDashboardRecyclerView.setAdapter(this.adapter);
    creatorDashboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));


    viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ps -> this.renderProjectAndStats(ps.first, ps.second));

    viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    startActivity(intent);
  }

  private void renderProjectAndStats(final @NonNull Project project, final @NonNull ProjectStats projectStats) {
    adapter.takeProjectAndStats(project, projectStats);
  }
}
