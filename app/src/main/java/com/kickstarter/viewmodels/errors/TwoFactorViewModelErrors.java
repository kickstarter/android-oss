package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface TwoFactorViewModelErrors {
  // Emits when a submitted TFA code does not match.
  Observable<String> tfaCodeMismatchError();

  // Emits when submitting TFA code errored for an unknown reason.
  Observable<Void> genericTfaError();
}
