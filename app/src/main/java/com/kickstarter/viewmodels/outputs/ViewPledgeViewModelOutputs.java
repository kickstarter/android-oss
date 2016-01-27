package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Backing;

import rx.Observable;

public interface ViewPledgeViewModelOutputs {
  Observable<Backing> backing();
}
