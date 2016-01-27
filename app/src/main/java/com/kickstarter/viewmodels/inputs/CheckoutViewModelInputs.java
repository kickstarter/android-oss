package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;

public interface CheckoutViewModelInputs {
  /**
   * Takes a url whenever a page has been intercepted by the web view.
   *
   * @param url The url that has been intercepted
   */
  void pageIntercepted(final @NonNull String url);
}
