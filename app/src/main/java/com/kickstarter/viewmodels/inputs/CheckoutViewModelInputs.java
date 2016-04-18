package com.kickstarter.viewmodels.inputs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface CheckoutViewModelInputs {
  /**
   * Takes a url whenever a page has been intercepted by the web view.
   *
   * @param url The url that has been intercepted
   */
  void pageIntercepted(final @NonNull String url);

  /**
   * Call when any back button is pressed.
   */
  void backButtonClicked();

  /**
   * Call when the activity obtains a base 64 payload from an android pay button in the webpage.
   */
  void takePayloadString(final @Nullable String payloadString);

  /**
   * Call when the user has clicked the confirm android pay button.
   */
  void confirmAndroidPayClicked();
}
