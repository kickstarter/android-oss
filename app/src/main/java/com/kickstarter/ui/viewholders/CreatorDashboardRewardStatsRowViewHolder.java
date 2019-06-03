package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.viewmodels.DashboardRewardStatsRowHolderViewModel;

import java.math.RoundingMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
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

  protected @BindString(R.string.dashboard_graphs_rewards_no_reward) String noRewardString;

  public CreatorDashboardRewardStatsRowViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new DashboardRewardStatsRowHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel.outputs.percentageOfTotalPledged()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentagePledgedForRewardTextView::setText);

    this.viewModel.outputs.projectAndRewardPledged()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedColumnValue);

    this.viewModel.outputs.rewardBackerCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.rewardBackerCountTextView::setText);

    this.viewModel.outputs.projectAndRewardMinimum()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setRewardMinimumText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope.RewardStats> projectAndRewardStats = requireNonNull((Pair<Project, ProjectStatsEnvelope.RewardStats>) data);
    this.viewModel.inputs.projectAndRewardStats(projectAndRewardStats);
  }

  private void setPledgedColumnValue(final @NonNull Pair<Project, Float> projectAndPledgedForReward) {
    final String goalString = this.ksCurrency
      .format(projectAndPledgedForReward.second, projectAndPledgedForReward.first, RoundingMode.DOWN);
    this.amountForRewardPledgedTextView.setText(goalString);
  }

  private void setRewardMinimumText(final @NonNull Pair<Project, Integer> projectAndMinimumForReward) {
    final String minimumString = IntegerUtils.isZero(projectAndMinimumForReward.second) ?
      this.noRewardString : this.ksCurrency.format(projectAndMinimumForReward.second, projectAndMinimumForReward.first, RoundingMode.HALF_UP);
    this.rewardMinimumTextView.setText(minimumString);
  }
}
