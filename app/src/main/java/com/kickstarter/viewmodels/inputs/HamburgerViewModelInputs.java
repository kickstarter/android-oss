package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.HamburgerNavigationItem;

public interface HamburgerViewModelInputs {
  void filterClicked(final @NonNull HamburgerNavigationItem item);
}
