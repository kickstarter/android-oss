package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import rx.Observable;

public interface WebViewViewModelOutputs {
  /**
   * String to display in the toolbar.
   */
  @NonNull Observable<String> toolbarTitle();

  /**
   * URL to load in the web view.
   */
  @NonNull Observable<String> url();
}
