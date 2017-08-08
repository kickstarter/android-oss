package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.viewmodels.DashboardRewardStatsRowHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardRewardStatsRowViewHolder extends KSViewHolder {

  private final DashboardRewardStatsRowHolderViewModel.ViewModel viewModel;
  protected @Bind(R.id.reward_minimum_text_view) TextView rewardMinimumTextView;
  protected @Bind(R.id.amount_pledged_for_reward_text_view) TextView amountForRewardPledgedTextView;
  protected @Bind(R.id.percentage_pledged_for_text_view) TextView percentagePledgedForRewardTextView;
  protected @Bind(R.id.reward_backer_count_text_view) TextView rewardBackerCountTextView;

  private KSCurrency ksCurrency;

  public CreatorDashboardRewardStatsRowViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new DashboardRewardStatsRowHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel.outputs.percentageOfTotalPledged()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percentagePledgedForRewardTextView::setText);

    this.viewModel.outputs.projectAndPledgedForReward()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedColumnValue);

    this.viewModel.outputs.rewardBackerCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardBackerCountTextView::setText);

    this.viewModel.outputs.rewardMinimum()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardMinimumTextView::setText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats = requireNonNull((Pair<Project, ProjectStatsEnvelope.RewardStats>) data);
    this.viewModel.inputs.projectAndRewardStats(projectAndRewardStats);
  }

  private void setPledgedColumnValue(final @NonNull Pair<Project, Float> projectAndPledgedForReward) {
    final String goalString = this.ksCurrency.format(
      projectAndPledgedForReward.second, projectAndPledgedForReward.first, false, true, RoundingMode.DOWN
    );
    this.amountForRewardPledgedTextView.setText(goalString);
  }
}
