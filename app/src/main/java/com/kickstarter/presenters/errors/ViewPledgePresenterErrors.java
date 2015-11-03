package com.kickstarter.presenters.errors;

import rx.Observable;

public interface ViewPledgePresenterErrors {
  public Observable<Void> backingLoadFailed();
}
