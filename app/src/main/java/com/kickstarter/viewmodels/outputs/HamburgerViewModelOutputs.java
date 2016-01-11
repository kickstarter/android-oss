package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.HamburgerNavigationData;

import rx.Observable;

public interface HamburgerViewModelOutputs {
  @NonNull Observable<HamburgerNavigationData> hamburgerNavigationData();
}
