package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface FacebookConfirmationViewModelErrors {
  Observable<String> signupError();
}
