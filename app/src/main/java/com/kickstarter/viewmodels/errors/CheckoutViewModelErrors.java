package com.kickstarter.viewmodels.errors;

import rx.Observable;

public interface CheckoutViewModelErrors {
  Observable<Integer> androidPayError();
}
