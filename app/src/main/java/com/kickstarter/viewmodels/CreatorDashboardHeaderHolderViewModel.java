package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;
import com.kickstarter.ui.viewholders.RewardViewHolder;

public class CreatorDashboardHeaderHolderViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> {

  public CreatorDashboardHeaderHolderViewModel(final @NonNull Environment environment) {
    super(environment);
  }
}
