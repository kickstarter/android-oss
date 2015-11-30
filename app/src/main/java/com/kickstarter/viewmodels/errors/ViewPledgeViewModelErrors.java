package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ViewPledgeViewModelErrors {
  public Observable<Void> backingLoadFailed();
}
