package com.kickstarter.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.kickstarter.R;
import com.kickstarter.databinding.RewardsItemViewBinding;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.kickstarter.ui.viewholders.RewardsItemViewHolder;

import java.util.List;

import static java.util.Collections.emptyList;

public final class RewardItemsAdapter extends KSAdapter {

  public RewardItemsAdapter() {
    addSection(emptyList());
  }

  public void rewardsItems(final @NonNull List<RewardsItem> rewardsItems) {
    setSection(0, rewardsItems);
    notifyDataSetChanged();
  }

  @Override
  protected int layout(final @NonNull SectionRow sectionRow) {
    return R.layout.rewards_item_view;
  }

    @Override
    protected @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull ViewGroup viewGroup) {
        return new RewardsItemViewHolder(RewardsItemViewBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false));
    }
}
