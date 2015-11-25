package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface ResetPasswordViewModelErrors {
  Observable<String> resetError();
}
