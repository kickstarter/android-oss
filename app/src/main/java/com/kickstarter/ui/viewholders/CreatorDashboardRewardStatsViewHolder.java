package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.viewmodels.CreatorDashboardRewardStatsHolderViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class CreatorDashboardRewardStatsViewHolder extends KSViewHolder {

  protected @Bind(R.id.dashboard_reward_stats_recycler_view) RecyclerView creatorDashboardRewardStatsRecyclerView;

  public CreatorDashboardRewardStatsViewHolder(final @NonNull View view) {
    super(view);
    final CreatorDashboardRewardStatsHolderViewModel.ViewModel viewModel;
    viewModel = new CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);


  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {

  }

}
