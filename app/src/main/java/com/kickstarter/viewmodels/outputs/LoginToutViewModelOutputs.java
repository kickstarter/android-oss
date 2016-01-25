package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import rx.Observable;

public interface LoginToutViewModelOutputs {
  @NonNull Observable<Void> returnResultAfterLoginSuccess();
  @NonNull Observable<Void> startDiscoveryAfterLoginSuccess();
}
