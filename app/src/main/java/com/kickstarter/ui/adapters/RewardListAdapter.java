package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.viewholders.RewardViewHolder;

import java.util.List;

public class RewardListAdapter extends RecyclerView.Adapter<RewardViewHolder> {

  // Rewards + project refactor

  private List<Reward> rewards;

  public RewardListAdapter(List<Reward> rewards) {
    this.rewards = rewards;
  }

  @Override
  public void onBindViewHolder(final RewardViewHolder viewHolder, final int i) {
    Log.d("TEST", this.toString());
    viewHolder.onBind(rewards.get(i));
  }

  @Override
  public RewardViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.
      from(viewGroup.getContext()).
      inflate(R.layout.reward_card_view, viewGroup, false);
    return new RewardViewHolder(view);
  }

  @Override
  public int getItemCount() {
    return rewards.size();
  }
}
