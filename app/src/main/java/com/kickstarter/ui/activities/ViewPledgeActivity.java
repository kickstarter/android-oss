package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.ui.adapters.RewardsItemAdapter;
import com.kickstarter.viewmodels.ViewPledgeViewModel;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(ViewPledgeViewModel.ViewModel.class)
public final class ViewPledgeActivity extends BaseActivity<ViewPledgeViewModel.ViewModel> {
  protected @Bind(R.id.view_pledge_avatar_image_view) ImageView avatarImageView;
  protected @Bind(R.id.view_pledge_backer_name) TextView backerNameTextView;
  protected @Bind(R.id.view_pledge_backer_number) TextView backerNumberTextView;
  protected @Bind(R.id.view_pledge_backing_amount_and_date_text_view) TextView backingAmountAndDateTextView;
  protected @Bind(R.id.view_pledge_backing_status) TextView backingStatusTextView;
  protected @Bind(R.id.view_pledge_estimated_delivery_section) View pledgeEstimatedDeliverySection;
  protected @Bind(R.id.project_context_creator_name) TextView projectContextCreatorNameTextView;
  protected @Bind(R.id.project_context_image_view) ImageView projectContextPhotoImageView;
  protected @Bind(R.id.project_context_project_name) TextView projectContextProjectNameTextView;
  protected @Bind(R.id.project_context_view) View projectContextView;
  protected @Bind(R.id.view_pledge_estimated_delivery) TextView pledgeEstimatedDeliveryTextView;
  protected @Bind(R.id.view_pledge_reward_minimum_and_description) TextView rewardMinimumAndDescriptionTextView;
  protected @Bind(R.id.view_pledge_rewards_item_recycler_view) RecyclerView rewardsItemRecyclerView;
  protected @Bind(R.id.view_pledge_rewards_item_section) View rewardsItemSection;
  protected @Bind(R.id.view_pledge_shipping_amount) TextView shippingAmountTextView;
  protected @Bind(R.id.view_pledge_shipping_location) TextView shippingLocationTextView;
  protected @Bind(R.id.view_pledge_shipping_section) View shippingSection;

  protected @BindString(R.string.backer_modal_backer_number) String backerNumberString;
  protected @BindString(R.string.backer_modal_status_backing_status) String backingStatusString;
  protected @BindString(R.string.backer_modal_pledge_amount_on_pledge_date) String pledgeAmountPledgeDateString;
  protected @BindString(R.string.backer_modal_reward_amount_reward_description) String rewardAmountRewardDescriptionString;
  protected @BindString(R.string.project_creator_by_creator) String creatorNameString;
  protected @BindString(R.string.project_view_pledge_status_canceled) String statusCanceled;
  protected @BindString(R.string.project_view_pledge_status_collected) String statusCollected;
  protected @BindString(R.string.project_view_pledge_status_dropped) String statusDropped;
  protected @BindString(R.string.project_view_pledge_status_errored) String statusErrored;
  protected @BindString(R.string.project_view_pledge_status_pledged) String statusPledged;

  private KSString ksString;

  @Override
  public void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.view_pledge_layout);
    ButterKnife.bind(this);

    final RewardsItemAdapter rewardsItemAdapter = new RewardsItemAdapter();
    this.rewardsItemRecyclerView.setAdapter(rewardsItemAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    this.rewardsItemRecyclerView.setLayoutManager(layoutManager);

    final Environment environment = environment();
    this.ksString = environment.ksString();

    this.viewModel.outputs.backerNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backerNameTextView::setText);

    this.viewModel.outputs.backerNumberTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBackerNumberTextViewText);

    this.viewModel.outputs.backingAmountAndDateTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ad -> setBackingAmountAndDateTextViewText(ad.first, ad.second));

    this.viewModel.outputs.backingStatus()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBackingStatusTextViewText);

    this.viewModel.outputs.creatorNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCreatorNameTextViewText);

    this.viewModel.outputs.estimatedDeliverySectionIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.pledgeEstimatedDeliverySection));

    this.viewModel.outputs.estimatedDeliverySectionTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.pledgeEstimatedDeliveryTextView::setText);

    this.viewModel.outputs.goBack()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> back());

    this.viewModel.outputs.loadBackerAvatar()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::loadBackerAvatar);

    this.viewModel.outputs.loadProjectPhoto()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url -> Picasso.with(this).load(url).into(this.projectContextPhotoImageView));

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectContextProjectNameTextView::setText);

    this.viewModel.outputs.rewardsItems()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rewardsItemAdapter::rewardsItems);

    this.viewModel.outputs.rewardsItemsAreHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.rewardsItemSection));

    this.viewModel.outputs.rewardMinimumAndDescriptionTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(md -> setRewardMinimumAndDescriptionTextViewText(md.first, md.second));

    this.viewModel.outputs.shippingAmountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.shippingAmountTextView::setText);

    this.viewModel.outputs.shippingLocationTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.shippingLocationTextView::setText);

    this.viewModel.outputs.shippingSectionIsHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.shippingSection));
  }

  @OnClick(R.id.project_context_view)
  protected void projectContextClicked() {
    this.viewModel.inputs.projectClicked();
  }

  @OnClick(R.id.view_pledge_view_messages_button)
  protected void viewMessagesButtonClicked() {
    this.viewModel.inputs.viewMessagesButtonClicked();
  }

  private void loadBackerAvatar(final @NonNull String url) {
    Picasso.with(this).load(url)
      .transform(new CircleTransformation())
      .into(this.avatarImageView);
  }

  private void setBackingAmountAndDateTextViewText(final @NonNull String amount, final @NonNull String date) {
    this.backingAmountAndDateTextView.setText(
      this.ksString.format(this.pledgeAmountPledgeDateString, "pledge_amount", amount, "pledge_date", date)
    );
  }

  private void setBackerNumberTextViewText(final @NonNull String sequence) {
    this.backerNumberTextView.setText(this.ksString.format(this.backerNumberString, "backer_number", sequence));
  }

  private void setBackingStatusTextViewText(final @NonNull String status) {
    final String str;
    switch (status) {
      case Backing.STATUS_CANCELED:
        str = statusCanceled;
        break;
      case Backing.STATUS_COLLECTED:
        str = statusCollected;
        break;
      case Backing.STATUS_DROPPED:
        str = statusDropped;
        break;
      case Backing.STATUS_ERRORED:
        str = statusErrored;
        break;
      case Backing.STATUS_PLEDGED:
        str = statusPledged;
        break;
      default:
        str = "";
    }

    this.backingStatusTextView.setText(this.ksString.format(this.backingStatusString, "backing_status", str));
  }

  private void setCreatorNameTextViewText(final @NonNull String name) {
    this.projectContextCreatorNameTextView.setText(this.ksString.format(this.creatorNameString, "creator_name", name));
  }

  private void setRewardMinimumAndDescriptionTextViewText(final @NonNull String minimum,
    final @NonNull String description) {

    this.rewardMinimumAndDescriptionTextView.setText(
      this.ksString.format(
        this.rewardAmountRewardDescriptionString, "reward_amount", minimum, "reward_description", description
      )
    );
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }
}
