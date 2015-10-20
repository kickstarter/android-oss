package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RewardViewHolder extends KsrViewHolder {
  public @Bind(R.id.pledge_minimum) TextView minimumTextView;
  public @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  public @Bind(R.id.reward_description) TextView descriptionTextView;
  public @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryTextView;
  public @Bind(R.id.selected) TextView selectedTextView;
  public @Bind(R.id.limited) TextView limitedTextView;
  public @Bind(R.id.all_gone) TextView allGoneTextView;
  public @Bind(R.id.white_overlay) View whiteOverlayView;

  @Inject Money money;

  private final Context context;
  private final Delegate delegate;
  private Project project;
  private Reward reward;

  public interface Delegate {
    void rewardClicked(@NonNull final RewardViewHolder viewHolder, @NonNull final Reward reward);
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
      context.getString(R.string.Pledge_or_more),
      money.formattedCurrency(reward.minimum(), project.currencyOptions())));
    backersCountTextView.setText(String.format(
      context.getString(R.string._backers),
      Integer.toString(reward.backersCount()))); // check Integer formatting
    descriptionTextView.setText(reward.description());
    estimatedDeliveryTextView.setText(
      reward.estimatedDeliveryOn().toString(DateTimeUtils.estimatedDeliveryOn()));

    toggleTextViews();
  }

  /**
   * Refreshes toggle-able views of each reward card so that it draws correctly each onBind.
   */
  public void invalidateCardView() {
    limitedTextView.setVisibility(View.GONE);
    allGoneTextView.setVisibility(View.GONE);
    whiteOverlayView.setVisibility(View.INVISIBLE);
    view.setClickable(true);
  }

  // Move this if- logic to ProjectPresenter
  public void toggleTextViews() {
    invalidateCardView();

    if (project.isBacking()) {
      view.setClickable(false);
    }

    if (reward.isAllGone()) {
      allGoneTextView.setVisibility(View.VISIBLE);
      whiteOverlayView.setVisibility(View.VISIBLE);
      view.setClickable(false);
    }
    else if (reward.isLimited()) {
      limitedTextView.setVisibility(View.VISIBLE);
      limitedTextView.setText(String.format(context.getString(R.string.Limited_left_of),
        reward.remaining(),
        reward.limit()
      ));
    }

    // todo: implement project.backing().rewardId()
    //    if (project.backingRewardId() == reward.id()) {
    //      selectedLabelTextView.setVisibility(View.VISIBLE);
    //      greenOverlayView.setVisibility(View.VISIBLE);
    //      view.setAlpha(0.4f);
    //    }
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.rewardClicked(this, reward);
  }
}
