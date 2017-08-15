package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardReferrerStatsAdapter;
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsHolderViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardReferrerStatsViewHolder extends KSViewHolder {

  private final CreatorDashboardReferrerStatsHolderViewModel.ViewModel viewModel;
  protected @Bind(R.id.dashboard_referrer_stats_recycler_view) RecyclerView referrerStatsRecyclerView;

  public CreatorDashboardReferrerStatsViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    final CreatorDashboardReferrerStatsAdapter referrerStatsAdapter = new CreatorDashboardReferrerStatsAdapter();
    this.referrerStatsRecyclerView.setAdapter(referrerStatsAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    this.referrerStatsRecyclerView.setLayoutManager(layoutManager);

    this.viewModel.outputs.projectAndReferrerStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(referrerStatsAdapter::takeProjectAndReferrerStats);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats = requireNonNull((Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>) data);
    this.viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats);
  }

  @Override
  protected void destroy() {
    super.destroy();
    this.referrerStatsRecyclerView.setAdapter(null);
  }
}
