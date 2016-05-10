package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.activities.ViewPledgeActivity;
import com.kickstarter.ui.adapters.RewardsItemAdapter;
import com.kickstarter.viewmodels.RewardViewModel;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;
import static rx.android.schedulers.AndroidSchedulers.mainThread;

public final class RewardViewHolder extends KSViewHolder {
  private final RewardViewModel viewModel;

  protected @Bind(R.id.reward_all_gone_header) View allGoneHeader;
  protected @Bind(R.id.reward_backers_text_view) TextView backersTextView;
  protected @Bind(R.id.reward_description_text_view) TextView descriptionTextView;
  protected @Bind(R.id.reward_estimated_delivery_date_section) View estimatedDeliveryDateSection;
  protected @Bind(R.id.reward_estimated_delivery_date_text_view) TextView estimatedDeliveryDateTextView;
  protected @Bind(R.id.reward_body_section) View bodySection;
  protected @Bind(R.id.reward_limit_and_remaining_section) LinearLayout limitAndRemainingSection;
  protected @Bind(R.id.reward_limit_and_remaining_text_view) TextView limitAndRemainingTextView;
  protected @Bind(R.id.reward_limit_divider) View limitDividerView;
  protected @Bind(R.id.reward_limit_header) View limitHeader;
  protected @Bind(R.id.reward_minimum_button) Button minimumButton;
  protected @Bind(R.id.reward_minimum_text_view) TextView minimumTextView;
  protected @Bind(R.id.reward_rewards_item_recycler_view) RecyclerView rewardsItemRecyclerView;
  protected @Bind(R.id.reward_rewards_item_section) View rewardsItemSection;
  protected @Bind(R.id.reward_selected_header) View selectedHeader;
  protected @Bind(R.id.reward_shipping_section) View shippingSection;
  protected @Bind(R.id.reward_shipping_summary_text_view) TextView shippingSummaryTextView;
  protected @Bind(R.id.reward_time_limit_section) View timeLimitSection;
  protected @Bind(R.id.reward_title_text_view) TextView titleTextView;
  protected @Bind(R.id.reward_view) View rewardView;
  protected @Bind(R.id.reward_usd_conversion_text_view) TextView usdConversionTextView;
  protected @Bind(R.id.reward_white_overlay_view) View whiteOverlayView;

  protected @BindColor(R.color.light_green) int lightGreenColor;
  protected @BindColor(R.color.white) int whiteColor;

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
    final RewardsItemAdapter rewardsItemAdapter = new RewardsItemAdapter();
    rewardsItemRecyclerView.setAdapter(rewardsItemAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    rewardsItemRecyclerView.setLayoutManager(layoutManager);

    RxView.clicks(rewardView)
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(__ -> viewModel.inputs.rewardClicked());

    viewModel.outputs.allGoneHeaderIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(allGoneHeader));

    viewModel.outputs.backersTextViewIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(backersTextView));

    viewModel.outputs.backersTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(this::setBackersTextView);

    viewModel.outputs.descriptionTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(descriptionTextView::setText);

    viewModel.outputs.estimatedDeliveryDateSectionIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(estimatedDeliveryDateSection));

    viewModel.outputs.estimatedDeliveryDateTextViewText()
      .map(DateTimeUtils::estimatedDeliveryOn)
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(estimatedDeliveryDateTextView::setText);

    viewModel.outputs.goToCheckout()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(pr -> goToCheckout(pr.first, pr.second));

    viewModel.outputs.goToViewPledge()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(this::goToViewPledge);

    viewModel.outputs.isClickable()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(rewardView::setClickable);

    viewModel.outputs.limitAndRemainingSectionIsCenterAligned()
      .map(a -> a ? Gravity.CENTER : Gravity.CENTER_VERTICAL)
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(limitAndRemainingSection::setGravity);

    viewModel.outputs.limitAndRemainingSectionIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(limitAndRemainingSection));

    viewModel.outputs.limitAndRemainingTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(lr -> setLimitAndRemainingTextView(lr.first, lr.second));

    viewModel.outputs.limitDividerIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setInvisible(limitDividerView));

    viewModel.outputs.limitHeaderIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(limitHeader));

    viewModel.outputs.minimumButtonText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(minimumButton::setText);

    viewModel.outputs.minimumButtonIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(minimumButton));

    viewModel.outputs.minimumTextViewIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(minimumTextView));

    viewModel.outputs.minimumTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(minimumTextView::setText);

    viewModel.outputs.minimumTitleTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(this::setTitleTextView);

    viewModel.outputs.rewardsItems()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(rewardsItemAdapter::rewardsItems);

    viewModel.outputs.rewardsItemsAreHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(rewardsItemSection));

    viewModel.outputs.rewardTitleTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(titleTextView::setText);

    viewModel.outputs.selectedHeaderIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(selectedHeader));

    viewModel.outputs.selectedOverlayIsHidden()
      .map(hidden -> hidden ? whiteColor : lightGreenColor)
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(bodySection::setBackgroundColor);

    viewModel.outputs.shippingSummarySectionIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(shippingSection));

    viewModel.outputs.shippingSummaryTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(shippingSummaryTextView::setText);

    viewModel.outputs.timeLimitSectionIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(timeLimitSection));

    viewModel.outputs.usdConversionTextViewIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setGone(usdConversionTextView));

    viewModel.outputs.usdConversionTextViewText()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(this::setUsdConversionTextView);

    viewModel.outputs.whiteOverlayIsHidden()
      .compose(bindToLifecycle())
      .observeOn(mainThread())
      .subscribe(ViewUtils.setInvisible(whiteOverlayView));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, Reward> projectAndReward = requireNonNull((Pair<Project, Reward>) data);
    final Project project = requireNonNull(projectAndReward.first, Project.class);
    final Reward reward = requireNonNull(projectAndReward.second, Reward.class);

    viewModel.inputs.projectAndReward(project, reward);
  }

  private void goToCheckout(final @NonNull Project project, final @NonNull Reward reward) {
    final Context context = context();
    final Intent intent = new Intent(context, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.TOOLBAR_TITLE, projectBackButtonString)
      .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward));

    context.startActivity(intent);
    transition(context, slideInFromRight());
  }

  private void goToViewPledge(final @NonNull Project project) {
    final Context context = context();
    final Intent intent = new Intent(context, ViewPledgeActivity.class)
      .putExtra(IntentKey.PROJECT, project);

    context.startActivity(intent);
    transition(context, slideInFromRight());
  }

  private void setBackersTextView(final int count) {
    final String backersCountText = ksString.format("rewards_info_backer_count_backers", count,
      "backer_count", NumberUtils.format(count));
    backersTextView.setText(backersCountText);
  }

  private void setTitleTextView(final @NonNull String minimum) {
    titleTextView.setText(ksString.format(
      pledgeRewardCurrencyOrMoreString,
      "reward_currency", minimum
    ));
  }

  private void setLimitAndRemainingTextView(final @NonNull String limit, final @NonNull String remaining) {
    limitAndRemainingTextView.setText(ksString.format(
      limitedRewardsRemainingString,
      "rewards_remaining", remaining,
      "reward_limit", limit
    ));
  }

  private void setUsdConversionTextView(final @NonNull String amount) {
    usdConversionTextView.setText(ksString.format(
      usdConversionString,
      "reward_amount", amount
    ));
  }

  @Override
  protected void destroy() {
    rewardsItemRecyclerView.setAdapter(null);
  }
}
