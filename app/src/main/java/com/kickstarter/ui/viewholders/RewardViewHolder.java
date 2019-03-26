package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
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
import com.kickstarter.ui.activities.BackingActivity;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.adapters.RewardsItemAdapter;
import com.kickstarter.viewmodels.RewardViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromRight;
import static com.kickstarter.libs.utils.TransitionUtils.transition;

public final class RewardViewHolder extends KSViewHolder {
  private final RewardViewModel.ViewModel viewModel;
  private final KSString ksString;
  private final Delegate delegate;

  protected @Bind(R.id.reward_all_gone_text_view) TextView allGoneTextView;
  protected @Bind(R.id.reward_backers_text_view) TextView backersTextView;
  protected @Bind(R.id.reward_usd_conversion_text_view) TextView conversionTextView;
  protected @Bind(R.id.reward_description_text_view) TextView descriptionTextView;
  protected @Bind(R.id.reward_estimated_delivery_date_section) View estimatedDeliveryDateSection;
  protected @Bind(R.id.reward_estimated_delivery_date_text_view) TextView estimatedDeliveryDateTextView;
  protected @Bind(R.id.reward_limit_and_remaining_text_view) TextView limitAndRemainingTextView;
  protected @Bind(R.id.reward_minimum_text_view) TextView minimumTextView;
  protected @Bind(R.id.reward_rewards_item_recycler_view) RecyclerView rewardsItemRecyclerView;
  protected @Bind(R.id.reward_rewards_item_section) View rewardsItemSection;
  protected @Bind(R.id.reward_selected_header) View selectedHeader;
  protected @Bind(R.id.reward_shipping_section) View shippingSection;
  protected @Bind(R.id.reward_shipping_summary_text_view) TextView shippingSummaryTextView;
  protected @Bind(R.id.reward_title_text_view) TextView titleTextView;
  protected @Bind(R.id.reward_view) CardView rewardView;
  protected @Bind(R.id.reward_white_overlay_view) View whiteOverlayView;

  protected @BindString(R.string.rewards_info_limited_rewards_remaining_left_of_reward_limit) String limitedRewardsRemainingString;
  protected @BindString(R.string.rewards_title_pledge_reward_currency_or_more) String pledgeRewardCurrencyOrMoreString;
  protected @BindString(R.string.project_back_button) String projectBackButtonString;
  protected @BindString(R.string.About_reward_amount) String currencyConversionString;
  private Reward reward;

  public interface Delegate {
    void rewardClicked(RewardViewHolder rewardViewHolder, Reward reward);
  }

  public RewardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    this.viewModel = new RewardViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);
    final RewardsItemAdapter rewardsItemAdapter = new RewardsItemAdapter();
    this.rewardsItemRecyclerView.setAdapter(rewardsItemAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    this.rewardsItemRecyclerView.setLayoutManager(layoutManager);

    RxView.clicks(this.rewardView)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.viewModel.inputs.rewardClicked());

    this.viewModel.outputs.allGoneTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.allGoneTextView));

    this.viewModel.outputs.backersTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backersTextView));

    this.viewModel.outputs.backersTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBackersTextView);

    this.viewModel.outputs.rewardDescriptionIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.descriptionTextView));

    this.viewModel.outputs.descriptionTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.descriptionTextView::setText);

    this.viewModel.outputs.estimatedDeliveryDateSectionIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.estimatedDeliveryDateSection));

    this.viewModel.outputs.estimatedDeliveryDateTextViewText()
      .map(DateTimeUtils::estimatedDeliveryOn)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.estimatedDeliveryDateTextView::setText);

    this.viewModel.outputs.startCheckoutActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pr -> startCheckoutActivity(pr.first, pr.second));

    this.viewModel.outputs.showPledgeFragment()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pr -> this.delegate.rewardClicked(this, this.reward));

    this.viewModel.outputs.startBackingActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startBackingActivity);

    this.viewModel.outputs.isClickable()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.rewardView::setClickable);

    this.viewModel.outputs.limitAndRemainingTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.limitAndRemainingTextView));

    this.viewModel.outputs.limitAndRemainingTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(lr -> setLimitAndRemainingTextView(lr.first, lr.second));

    this.viewModel.outputs.minimumTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setMinimumTextView);

    this.viewModel.outputs.rewardsItemList()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardsItemAdapter::rewardsItems);

    this.viewModel.outputs.rewardsItemsAreGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.rewardsItemSection));

    this.viewModel.outputs.selectedHeaderIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.selectedHeader));

    this.viewModel.outputs.shippingSummarySectionIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.shippingSection));

    this.viewModel.outputs.shippingSummaryTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.shippingSummaryTextView::setText);

    this.viewModel.outputs.titleTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.titleTextView));

    this.viewModel.outputs.titleTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.titleTextView::setText);

    this.viewModel.outputs.conversionTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.conversionTextView));

    this.viewModel.outputs.conversionTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setConversionTextView);

    this.viewModel.outputs.whiteOverlayIsInvisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setInvisible(this.whiteOverlayView));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, Reward> projectAndReward = requireNonNull((Pair<Project, Reward>) data);
    final Project project = requireNonNull(projectAndReward.first, Project.class);
    this.reward = requireNonNull(projectAndReward.second, Reward.class);

    this.viewModel.inputs.projectAndReward(project, this.reward);
  }

  private void setBackersTextView(final int count) {
    final String backersCountText = this.ksString.format("rewards_info_backer_count_backers", count,
      "backer_count", NumberUtils.format(count));
    this.backersTextView.setText(backersCountText);
  }

  private void setMinimumTextView(final @NonNull String minimum) {
    this.minimumTextView.setText(this.ksString.format(
      this.pledgeRewardCurrencyOrMoreString,
      "reward_currency", minimum
    ));
  }

  private void setLimitAndRemainingTextView(final @NonNull String limit, final @NonNull String remaining) {
    this.limitAndRemainingTextView.setText(this.ksString.format(
      this.limitedRewardsRemainingString,
      "rewards_remaining", remaining,
      "reward_limit", limit
    ));
  }

  private void setConversionTextView(final @NonNull String amount) {
    this.conversionTextView.setText(this.ksString.format(
      this.currencyConversionString,
      "reward_amount", amount
    ));
  }

  private void startBackingActivity(final @NonNull Project project) {
    final Context context = context();
    final Intent intent = new Intent(context, BackingActivity.class)
      .putExtra(IntentKey.PROJECT, project);

    context.startActivity(intent);
    transition(context, slideInFromRight());
  }

  private void startCheckoutActivity(final @NonNull Project project, final @NonNull Reward reward) {
    final Context context = context();
    final Intent intent = new Intent(context, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.TOOLBAR_TITLE, this.projectBackButtonString)
      .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward));

    context.startActivity(intent);
    transition(context, slideInFromRight());
  }

  @Override
  protected void destroy() {
    this.rewardsItemRecyclerView.setAdapter(null);
  }
}
