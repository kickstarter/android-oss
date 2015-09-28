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
  protected @Bind(R.id.pledge_minimum) TextView minimumTextView;
  protected @Bind(R.id.reward_backers_count) TextView backersCountTextView;
  protected @Bind(R.id.reward_description) TextView descriptionTextView;
  protected @Bind(R.id.estimated_delivery_date) TextView estimatedDeliveryTextView;
  @Inject Money money;

  private final Delegate delegate;
  private Project project;
  private Reward reward;

  public interface Delegate {
    void rewardCardClick(final RewardViewHolder viewHolder, final Reward reward);
  }

  public RewardViewHolder(final View view, final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final Object datum) {
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
  }

  // todo:
  @Override
  public void onClick(final View view) {
    //    Uri rewardSelectedUrl = Uri.withAppendedPath(project.urls().web().project(),
    //      "/pledge/new?backing[backer_reward_id]={id}&clicked_reward=true");

    //    [backer_reward_id] = {id}

    //    something like:

    //    backing: {
    //      backer_reward_id: 1
    //    }

    /*
    ENDPOINT:

    project.urls.web.project + /pledge/new?backing[backer_reward_id]={id}&clicked_reward=true

    */

    // must we go through the delegate here?
    // we can, to go through the project activity, which then DisplayWebViewActivity
    // can be used
  }
}
