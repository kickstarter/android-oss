package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ViewPledgeViewModelErrors {
  Observable<Void> backingLoadFailed();
}
