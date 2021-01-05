package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.libs.utils.extensions.StringExtKt;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardRewardStatsAdapter;
import com.kickstarter.viewmodels.CreatorDashboardRewardStatsHolderViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardRewardStatsViewHolder extends KSViewHolder {
  private final CreatorDashboardRewardStatsHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.dashboard_reward_stats_empty_text_view) TextView emptyTextView;
  protected @Bind(R.id.dashboard_reward_stats_pledged_view) View pledgedColumnView;
  protected @Bind(R.id.dashboard_reward_stats_truncated_text_view) TextView truncatedTextView;
  protected @Bind(R.id.dashboard_reward_stats_recycler_view) RecyclerView rewardStatsRecyclerView;
  protected @Bind(R.id.dashboard_reward_title) TextView rewardsTitleTextView;

  protected @BindString(R.string.dashboard_graphs_rewards_top_rewards) String topRewardsString;
  protected @BindString(R.string.Top_ten_rewards) String topTenRewardsString;

  public CreatorDashboardRewardStatsViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardRewardStatsHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    final CreatorDashboardRewardStatsAdapter rewardStatsAdapter = new CreatorDashboardRewardStatsAdapter();
    this.rewardStatsRecyclerView.setAdapter(rewardStatsAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    this.rewardStatsRecyclerView.setLayoutManager(layoutManager);

    this.viewModel.outputs.projectAndRewardStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardStatsAdapter::takeProjectAndStats);

    this.viewModel.outputs.rewardsStatsListIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::toggleRecyclerViewAndEmptyStateVisibility);

    this.viewModel.outputs.rewardsStatsTruncatedTextIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> ViewUtils.setGone(this.truncatedTextView, gone));

    this.viewModel.outputs.rewardsTitleIsTopTen()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setTitleCopy);
  }

  private void setTitleCopy(final boolean referrersTitleIsTopTen) {
    final String formattedTopRewards = StringExtKt.sentenceCase(this.topRewardsString);
    this.rewardsTitleTextView.setText(referrersTitleIsTopTen ? this.topTenRewardsString : formattedTopRewards);
  }

  private void toggleRecyclerViewAndEmptyStateVisibility(final @NonNull Boolean gone) {
    ViewUtils.setGone(this.rewardStatsRecyclerView, gone);
    ViewUtils.setGone(this.emptyTextView, !gone);
  }

  @OnClick(R.id.dashboard_reward_stats_pledged_view)
  public void pledgedColumnTitleClicked() {
    this.viewModel.inputs.pledgedColumnTitleClicked();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, List<ProjectStatsEnvelope.RewardStats>> projectAndRewardStats = requireNonNull((Pair<Project, List<ProjectStatsEnvelope.RewardStats>>) data);
    this.viewModel.inputs.projectAndRewardStatsInput(projectAndRewardStats);
  }

  @Override
  protected void destroy() {
    super.destroy();
    this.rewardStatsRecyclerView.setAdapter(null);
  }
}
