package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.kickstarter.libs.models.AndroidPayPayload;
import com.kickstarter.models.Project;

import rx.Observable;

public interface CheckoutViewModelOutputs {
  /**
   *  The project associated with the current checkout.
   */
  Observable<Project> project();

  /**
   *  The title to display to the user.
   */
  Observable<String> title();

  /**
   * The URL the web view should load, if its state has been destroyed.
   */
  Observable<String> url();

  /**
   * Emits a boolean that determines if the android pay confirmation should be shown or not.
   */
  Observable<Boolean> displayAndroidPayConfirmation();

  /**
   * Emits a masked wallet and payload when the confirmation view should be updated with the newest data.
   */
  Observable<Pair<MaskedWallet, AndroidPayPayload>> updateAndroidPayConfirmation();

  /**
   * Emits when the activity should pop itself off the navigation stack.
   */
  Observable<Void> popActivityOffStack();

  /**
   * Emits a payload whenever an android pay sheet should be displayed.
   *
   * Can emit `null`, which means a prompt should not be displayed.
   */
  Observable<AndroidPayPayload> showAndroidPaySheet();

  /**
   * Emits a full wallet when android pay has been completely confirmed, and we are now ready to interact
   * with our payment processor.
   */
  Observable<FullWallet> completeAndroidPay();

  /**
   * Emits a boolean if this device is capable of android pay.
   */
  Observable<Boolean> isAndroidPayAvailable();

  /**
   * Emits the masked wallet and android pay payload when it is time to attempt to convert the masked
   * wallet into a full wallet.
   */
  Observable<Pair<MaskedWallet, AndroidPayPayload>> attemptAndroidPayConfirmation();
}
