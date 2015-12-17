package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class RewardViewHolder extends KSViewHolder {
  public @Bind(R.id.pledge_minimum) TextView minimumTextView;
  public @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  public @Bind(R.id.reward_description) TextView descriptionTextView;
  public @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryTextView;
  public @Bind(R.id.green_overlay) View greenOverlayView;
  public @Bind(R.id.selected) TextView selectedTextView;
  public @Bind(R.id.limited) TextView limitedTextView;
  public @Bind(R.id.all_gone) TextView allGoneTextView;
  public @Bind(R.id.white_overlay) View whiteOverlayView;
  public @Bind(R.id.shipping_destination) TextView shippingDestinationTextView;
  public @Bind(R.id.shipping_summary) TextView shippingSummaryTextView;

  protected @BindString(R.string.rewards_info_limited_rewards_remaining_left_of_reward_limit) String limitedRewardsRemainingString;

  @Inject KSCurrency ksCurrency;
  @Inject KSString ksString;

  private final Context context;
  private final Delegate delegate;
  private Project project;
  private Reward reward;

  public interface Delegate {
    void rewardClicked(RewardViewHolder viewHolder, Reward reward);
  }

  public RewardViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    final Pair<Project, Reward> projectAndReward = (Pair<Project, Reward>) datum;
    project = projectAndReward.first;
    reward = projectAndReward.second;

    minimumTextView.setText(String.format(
      context.getString(R.string.___Pledge_or_more),
      ksCurrency.format(reward.minimum(), project)));

    final Integer backersCount = reward.backersCount();
    final String backersCountText = (backersCount != null) ?
      ksString.format("rewards_info_backer_count_backers", backersCount,
        "backer_count", Integer.toString(backersCount)) :
      "";
    backersCountTextView.setText(backersCountText);

    descriptionTextView.setText(reward.description());
    estimatedDeliveryTextView.setText(
      reward.estimatedDeliveryOn().toString(DateTimeUtils.estimatedDeliveryOn()));

    toggleAllGoneRewardView();
    toggleClickableReward();
    toggleLimitedRewardView();
    toggleSelectedRewardView();
    toggleShippingDestinationView();
  }

  public void toggleAllGoneRewardView() {
    if (reward.isAllGone()) {
      allGoneTextView.setVisibility(View.VISIBLE);
      whiteOverlayView.setVisibility(View.VISIBLE);
    } else {
      allGoneTextView.setVisibility(View.GONE);
      whiteOverlayView.setVisibility(View.INVISIBLE);
    }
  }

  public void toggleLimitedRewardView() {
    if (reward.isLimited()) {
      limitedTextView.setVisibility(View.VISIBLE);
      limitedTextView.setText(ksString.format(limitedRewardsRemainingString,
        "rewards_remaining", ObjectUtils.toString(reward.remaining()),
        "reward_limit", ObjectUtils.toString(reward.limit()))
      );
    } else {
      limitedTextView.setVisibility(View.GONE);
    }
  }

  public void toggleSelectedRewardView() {
    if (project.isBackingRewardId(reward.id())) {
      greenOverlayView.setVisibility(View.VISIBLE);
      selectedTextView.setVisibility(View.VISIBLE);
    } else {
      greenOverlayView.setVisibility(View.INVISIBLE);
      selectedTextView.setVisibility(View.GONE);
    }
  }

  public void toggleShippingDestinationView() {
    if (reward.shippingSummary() != null) {
      shippingDestinationTextView.setVisibility(View.VISIBLE);
      shippingSummaryTextView.setVisibility(View.VISIBLE);
      shippingSummaryTextView.setText(reward.shippingSummary());
    } else {
      shippingDestinationTextView.setVisibility(View.GONE);
      shippingSummaryTextView.setVisibility(View.GONE);
    }
  }

  public void toggleClickableReward() {
    if (project.isBacking()) {
      view.setClickable(false);
    }
    else if (!project.isLive()) {
      view.setClickable(false);
    }
    else if (reward.isAllGone()) {
      view.setClickable(false);
    }
    else {
      view.setClickable(true);
    }
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.rewardClicked(this, reward);
  }
}
