package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Reward;
import com.kickstarter.presenters.ProjectDetailPresenter;
import com.kickstarter.ui.view_holders.RewardListViewHolder;

import java.util.List;

public class RewardListAdapter extends RecyclerView.Adapter<RewardListViewHolder> {
  private List<Reward> rewards;

  public RewardListAdapter(List<Reward> rewards) {
    this.rewards = rewards;
  }

  @Override
  public void onBindViewHolder(final RewardListViewHolder viewHolder, final int i) {
    viewHolder.onBind(rewards.get(i));
  }

  @Override
  public RewardListViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.
        from(viewGroup.getContext()).
        inflate(R.layout.reward_card_view, viewGroup, false);
    return new RewardListViewHolder(view);
  }

  @Override
  public int getItemCount() {
    return rewards.size();
  }
}
