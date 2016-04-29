package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.TransitionUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.viewmodels.RewardViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class RewardViewHolder extends KSViewHolder {
  private final RewardViewModel viewModel;

  protected @Bind(R.id.all_gone) TextView allGoneTextView;
  protected @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  protected @Bind(R.id.reward_description) TextView descriptionTextView;
  protected @Bind(R.id.estimated_section) View estimatedSectionView;
  protected @Bind(R.id.estimated_delivery) TextView estimatedDeliveryTextView;
  protected @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryDateTextView;
  protected @Bind(R.id.green_overlay) View greenOverlayView;
  protected @Bind(R.id.limited) TextView limitedTextView;
  protected @Bind(R.id.pledge_minimum) TextView minimumTextView;
  protected @Bind(R.id.reward_card_view) View rewardCardView;
  protected @Bind(R.id.select_text_view) @Nullable View selectTextView;
  protected @Bind(R.id.selected) TextView selectedTextView;
  protected @Bind(R.id.shipping_destination) TextView shippingDestinationTextView;
  protected @Bind(R.id.shipping_section) View shippingSectionView;
  protected @Bind(R.id.shipping_summary) TextView shippingSummaryTextView;
  protected @Bind(R.id.usd_conversion_text_view) TextView usdConversionTextView;
  protected @Bind(R.id.white_overlay) View whiteOverlayView;

  protected @BindString(R.string.rewards_info_limited_rewards_remaining_left_of_reward_limit) String limitedRewardsRemainingString;
  protected @BindString(R.string.rewards_title_pledge_reward_currency_or_more) String pledgeRewardCurrencyOrMoreString;
  protected @BindString(R.string.project_back_button) String projectBackButtonString;
  protected @BindString(R.string.rewards_title_about_amount_usd) String usdConversionString;

  private final KSString ksString;

  public RewardViewHolder(final @NonNull View view) {
    super(view);

    ksString = environment().ksString();
    viewModel = new RewardViewModel(environment());

    ButterKnife.bind(this, view);

    RxView.clicks(rewardCardView)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> viewModel.inputs.rewardClicked());

    viewModel.outputs.allGoneIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(allGoneTextView));

    viewModel.outputs.allGoneIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setInvisible(whiteOverlayView));

    viewModel.outputs.backers()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setBackersCountTextView);

    viewModel.outputs.backersIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(backersCountTextView));

    viewModel.outputs.description()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(descriptionTextView::setText);

    viewModel.outputs.estimatedDelivery()
      .map(DateTimeUtils::estimatedDeliveryOn)
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(estimatedDeliveryDateTextView::setText);

    viewModel.outputs.estimatedDeliveryIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(estimatedSectionView));

    viewModel.outputs.limitIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(limitedTextView));

    viewModel.outputs.limitAndRemaining()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(lr -> setLimitAndRemainingTextView(lr.first, lr.second));

    viewModel.outputs.minimum()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setMinimumTextView);

    viewModel.outputs.clickable()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(rewardCardView::setClickable);

    viewModel.outputs.selectedRewardIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setInvisible(greenOverlayView));

    viewModel.outputs.selectedRewardIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(selectedTextView));

    viewModel.outputs.shippingSummary()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(shippingSummaryTextView::setText);

    viewModel.outputs.shippingSummaryIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(shippingSectionView));

    viewModel.outputs.goToCheckout()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pr -> goToCheckout(pr.first, pr.second));

    viewModel.outputs.usdConversionIsHidden()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(ViewUtils.setGone(usdConversionTextView));

    viewModel.outputs.usdConversion()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setUsdConversionTextView);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, Reward> projectAndReward = requireNonNull((Pair<Project, Reward>) data);
    final Project project = requireNonNull(projectAndReward.first, Project.class);
    final Reward reward = requireNonNull(projectAndReward.second, Reward.class);

    viewModel.inputs.projectAndReward(project, reward);
  }

  private void setBackersCountTextView(final int count) {
    final String backersCountText = ksString.format("rewards_info_backer_count_backers", count,
      "backer_count", NumberUtils.format(count));
    backersCountTextView.setText(backersCountText);
  }

  private void setLimitAndRemainingTextView(final @NonNull String limit, final @NonNull String remaining) {
    limitedTextView.setText(ksString.format(
      limitedRewardsRemainingString,
      "rewards_remaining", remaining,
      "reward_limit", limit
    ));
  }

  private void setMinimumTextView(final @NonNull String minimum) {
    minimumTextView.setText(ksString.format(
      pledgeRewardCurrencyOrMoreString,
      "reward_currency", minimum
    ));
  }

  private void goToCheckout(final @NonNull Project project, final @NonNull Reward reward) {
    final Context context = context();
    final Intent intent = new Intent(context, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.TOOLBAR_TITLE, projectBackButtonString)
      .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward));

    context.startActivity(intent);
    TransitionUtils.transition(context, TransitionUtils.slideInFromRight());
  }

  public void setUsdConversionTextView(final @NonNull String amount) {
    usdConversionTextView.setText(ksString.format(
      usdConversionString,
      "reward_amount", amount
    ));
  }
}
