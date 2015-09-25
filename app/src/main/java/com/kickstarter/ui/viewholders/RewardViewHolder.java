package com.kickstarter.ui.viewholders;

import android.content.Context;
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
  protected @Bind(R.id.pledge_minimum) TextView minimum;
  protected @Bind(R.id.reward_backers_count) TextView backers_count;
  protected @Bind(R.id.reward_description) TextView description;
  protected @Bind(R.id.estimated_delivery) TextView estimated_delivery;

  @Inject Money money;

  private Project project;
  private Reward reward;

  public RewardViewHolder(final View view) {
    super(view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final Object datum) {
    final Pair<Project, Reward> projectAndReward = (Pair<Project, Reward>) datum;
    project = projectAndReward.first;
    reward = projectAndReward.second;

    final Context context = view.getContext();

    minimum.setText(String.format(
      context.getString(R.string.Pledge_or_more),
      money.formattedCurrency(reward.minimum(), project.currencyOptions())));
    backers_count.setText(String.format(
        context.getString(R.string._backers),
        reward.backersCount().toString())); // check Integer formatting
    description.setText(reward.description());
    estimated_delivery.setText(
      reward.estimatedDeliveryOn().toString(DateTimeUtils.estimatedDeliveryOn()));
  }
}
