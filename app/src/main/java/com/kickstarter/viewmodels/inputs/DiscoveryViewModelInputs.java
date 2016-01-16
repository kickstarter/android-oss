package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

public interface DiscoveryViewModelInputs {
  void nextPage();
  void filterButtonClicked();
  void initializer(final @NonNull DiscoveryParams params);
}
