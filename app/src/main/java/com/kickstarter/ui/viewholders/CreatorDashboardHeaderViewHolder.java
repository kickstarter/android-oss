package com.kickstarter.ui.viewholders;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.viewmodels.CreatorDashboardHeaderHolderViewModel;

public final class CreatorDashboardHeaderViewHolder extends KSViewHolder {

  private final CreatorDashboardHeaderHolderViewModel viewModel;

  public CreatorDashboardHeaderViewHolder(final @NonNull View view) {
    super(view);

    viewModel = new CreatorDashboardHeaderHolderViewModel(environment());
  }

  public void bindData(final @Nullable Object data) throws Exception {
//    viewmodel.inputs.project
  }

}
