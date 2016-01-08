package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class RewardViewHolder extends KSViewHolder {
  protected @Bind(R.id.pledge_minimum) TextView minimumTextView;
  protected @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  protected @Bind(R.id.reward_description) TextView descriptionTextView;
  protected @Bind(R.id.estimated_delivery) TextView estimatedDeliveryTextView;
  protected @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryDateTextView;
  protected @Bind(R.id.green_overlay) View greenOverlayView;
  protected @Bind(R.id.selected) TextView selectedTextView;
  protected @Bind(R.id.limited) TextView limitedTextView;
  protected @Bind(R.id.all_gone) TextView allGoneTextView;
  protected @Bind(R.id.white_overlay) View whiteOverlayView;
  protected @Bind(R.id.shipping_destination) TextView shippingDestinationTextView;
  protected @Bind(R.id.shipping_summary) TextView shippingSummaryTextView;
  protected @Bind(R.id.usd_conversion_text_view) TextView usdConversionTextView;

  protected @BindString(R.string.rewards_info_limited_rewards_remaining_left_of_reward_limit) String limitedRewardsRemainingString;
  protected @BindString(R.string.rewards_title_pledge_reward_currency_or_more) String pledgeRewardCurrencyOrMoreString;
  protected @BindString(R.string.rewards_title_about_amount_usd) String usdConversionString;

  @Inject KSCurrency ksCurrency;
  @Inject KSString ksString;

  private final Context context;
  private final Delegate delegate;
  private Project project;
  private Reward reward;
  private String configCountry;

  public interface Delegate {
    void rewardViewHolderClicked(RewardViewHolder viewHolder, Reward reward);
  }

  public RewardViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    final List<Object> projectRewardUser = (List<Object>) datum;
    project = (Project) projectRewardUser.get(0);
    reward = (Reward) projectRewardUser.get(1);
    configCountry = (String) projectRewardUser.get(2);

    minimumTextView.setText(ksString.format(
      pledgeRewardCurrencyOrMoreString,
      "reward_currency",
      ksCurrency.format(reward.minimum(), project)
    ));

    final Integer backersCount = reward.backersCount();
    final String backersCountText = (backersCount != null) ?
      ksString.format("rewards_info_backer_count_backers", backersCount,
        "backer_count", NumberUtils.format(backersCount)) :
      "";
    backersCountTextView.setText(backersCountText);
    descriptionTextView.setText(reward.description());

    toggleAllGoneRewardView();
    toggleClickableReward();
    toggleEstimatedDeliveryView();
    toggleLimitedRewardView();
    toggleSelectedRewardView();
    toggleShippingDestinationView();
    toggleUsdConversionView();
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

  public void toggleClickableReward() {
    if (project.isBacking()) {
      view.setClickable(false);
    }
    else if (!project.isLive()) {
      view.setClickable(false);
    }
    else if (reward.isAllGone()) {
      view.setClickable(false);
    } else {
      view.setClickable(true);
    }
  }

  public void toggleEstimatedDeliveryView() {
    if (reward.hasEstimatedDelivery()) {
      estimatedDeliveryTextView.setVisibility(View.VISIBLE);
      estimatedDeliveryDateTextView.setVisibility(View.VISIBLE);
      estimatedDeliveryDateTextView.setText(
        DateTimeUtils.estimatedDeliveryOn(reward.estimatedDeliveryOn())
      );
    } else {
      estimatedDeliveryTextView.setVisibility(View.GONE);
      estimatedDeliveryDateTextView.setVisibility(View.GONE);
    }
  }

  public void toggleLimitedRewardView() {
    if (reward.isLimited()) {
      limitedTextView.setVisibility(View.VISIBLE);
      limitedTextView.setText(ksString.format(
        limitedRewardsRemainingString,
        "rewards_remaining",
        ObjectUtils.toString(reward.remaining()),
        "reward_limit", ObjectUtils.toString(reward.limit())
      ));
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

  public void toggleUsdConversionView() {
    if (ProjectUtils.isUSUserViewingNonUSProject(configCountry, project.country())) {
      usdConversionTextView.setVisibility(View.VISIBLE);
      usdConversionTextView.setText(ksString.format(
          usdConversionString,
          "reward_amount",
          ksCurrency.format(reward.minimum(), project, true, true))
      );
    } else {
      usdConversionTextView.setVisibility(View.GONE);
    }
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.rewardViewHolderClicked(this, reward);
  }
}
