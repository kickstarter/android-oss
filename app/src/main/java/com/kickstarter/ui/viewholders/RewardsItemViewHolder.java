package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.RewardsItem;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class RewardsItemViewHolder extends KSViewHolder {
  private final KSString ksString;

  protected @Bind(R.id.rewards_item_title) TextView titleTextView;

  public RewardsItemViewHolder(final @NonNull View view) {
    super(view);
    ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final RewardsItem rewardsItem = requireNonNull((RewardsItem) data);

    final String title = ksString.format("rewards_info_item_quantity_title", rewardsItem.quantity(),
      "quantity", ObjectUtils.toString(rewardsItem.quantity()),
      "title", rewardsItem.item().name()
    );
    titleTextView.setText(title);
  }
}
