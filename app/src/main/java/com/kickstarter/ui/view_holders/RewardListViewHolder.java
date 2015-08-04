package com.kickstarter.ui.view_holders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.models.Reward;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class RewardListViewHolder extends RecyclerView.ViewHolder {
  protected @InjectView(R.id.pledge_minimum) TextView minimum;
  protected @InjectView(R.id.reward_backers_count) TextView backers_count;
  protected @InjectView(R.id.reward_description) TextView description;
  protected @InjectView(R.id.estimated_delivery) TextView estimated_delivery;

  protected View view;
  protected Reward reward;

  public RewardListViewHolder(View view) {
    super(view);
    this.view = view;

    Log.d("TEST", "entered RLVH");
    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.inject(this, view);

    view.setOnClickListener((View v) -> {
      Log.d("TEST", "Reward " + reward.id() + " selected");
    });
  }

  public void onBind(final Reward reward) {
    this.reward = reward;
    Log.d("TEST", "Reward received: " + reward.description());

    // todo: handle null values of rewards[0]
    if (reward.id() != 0) {
      minimum.setText("Pledge " + Integer.toString(reward.minimum()) + " or more");
      backers_count.setText(Integer.toString(reward.backers_count()) + " backers");
      description.setText(reward.description());
      estimated_delivery.setText(reward.estimated_delivery_on().toString(DateTimeUtils.estimatedDeliveryOn()));
    }
  }
}
