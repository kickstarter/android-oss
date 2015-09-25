package com.kickstarter.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.models.Reward;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Bind;

public class RewardViewHolder extends RecyclerView.ViewHolder {
  protected @Bind(R.id.pledge_minimum) TextView minimum;
  protected @Bind(R.id.reward_backers_count) TextView backers_count;
  protected @Bind(R.id.reward_description) TextView description;
  protected @Bind(R.id.estimated_delivery) TextView estimated_delivery;

  @Inject Money money;

  protected View view;
  protected Reward reward;

  public RewardViewHolder(View view) {
    super(view);
    this.view = view;

    Log.d("TEST", "entered RLVH");
//    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);

    view.setOnClickListener((View v) -> {
      Log.d("TEST", "Reward " + reward.id() + " selected");
    });
  }

  // the datum will be reward + project
  // pass in the view...
  public void onBind(final Reward reward) {
    this.reward = reward;
    Log.d("TEST", "Reward received: " + reward.description());

    // todo: handle null values of rewards[0]
    // filter out
    if (reward.id() != 0) {

//      minimum.setText(String.format(view.getContext().getString(R.string.Pledge_or_more),
//        money.formattedCurrency(reward.minimum(), project.currencyOptions())
//      ));

      minimum.setText("Pledge " + Integer.toString(reward.minimum()) + " or more");
      backers_count.setText(Integer.toString(reward.backersCount()) + " backers");
      description.setText(reward.description());
      estimated_delivery.setText(reward.estimatedDeliveryOn().toString(DateTimeUtils.estimatedDeliveryOn()));
    }
  }
}