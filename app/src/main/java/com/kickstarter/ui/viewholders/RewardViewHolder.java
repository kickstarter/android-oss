package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class RewardViewHolder extends KsrViewHolder {
  protected @Bind(R.id.pledge_minimum) TextView minimumTextView;
  protected @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  protected @Bind(R.id.reward_description) TextView descriptionTextView;
  protected @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryTextView;
  protected @Bind(R.id.selected_label) TextView selectedLabelTextView;
  protected @Bind(R.id.limited_label) TextView limitedLabelTextView;
  protected @Bind(R.id.all_gone_label) TextView allGoneLabelTextView;
  protected @Bind(R.id.green_overlay) View greenOverlayView;

  @Inject Money money;

  private final Delegate delegate;
  private Project project;
  private Reward reward;

  public interface Delegate {
    void rewardClicked(@NonNull final RewardViewHolder viewHolder, @NonNull final Reward reward);
  }

  public RewardViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    final Pair<Project, Reward> projectAndReward = (Pair<Project, Reward>) datum;
    project = projectAndReward.first;
    reward = projectAndReward.second;

    final Context context = view.getContext();

    minimumTextView.setText(String.format(
      context.getString(R.string.Pledge_or_more),
      money.formattedCurrency(reward.minimum(), project.currencyOptions())));
    backersCountTextView.setText(String.format(
      context.getString(R.string._backers),
      Integer.toString(reward.backersCount()))); // check Integer formatting
    descriptionTextView.setText(reward.description());
    estimatedDeliveryTextView.setText(
      reward.estimatedDeliveryOn().toString(DateTimeUtils.estimatedDeliveryOn()));
    setRewardCardLabels(context);
  }

  public void setRewardCardLabels(@NonNull final Context context) {

    // Move this if logic to presenter
    if (reward.isLimited()) {
      limitedLabelTextView.setVisibility(View.VISIBLE);
      limitedLabelTextView.setText(String.format(context.getString(
          R.string.Limited_left_of),
        reward.remaining(),
        reward.limit()
      ));
    }

    else if (reward.isAllGone()) {
      allGoneLabelTextView.setVisibility(View.VISIBLE);
      view.setAlpha(0.2f);
      view.setClickable(false);
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
