package com.kickstarter.viewmodels.outputs;

import android.support.annotation.NonNull;

import com.kickstarter.models.Project;

import rx.Observable;

public interface CheckoutViewModelOutputs {
  /**
   *  The project associated with the current checkout.
   */
  @NonNull Observable<Project> project();

  /**
   *  The title to display to the user.
   */
  @NonNull Observable<String> title();

  /**
   * The URL the web view should load, if its state has been destroyed.
   */
  @NonNull Observable<String> url();
}
