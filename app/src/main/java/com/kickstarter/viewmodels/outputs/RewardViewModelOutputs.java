package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;

import rx.Observable;

public interface RewardViewModelOutputs {
  /**
   * Show an indicator that a reward's limit has been reached.
   */
  Observable<Boolean> allGoneIsHidden();

  /**
   * Show the number of backers.
   */
  Observable<Integer> backers();

  /**
   * Returns `true` if the estimated delivery section should be hidden, `false` otherwise.
   */
  Observable<Boolean> backersIsHidden();

  /**
   * Returns `true` if the user can click the reward, `false` otherwise.
   */
  Observable<Boolean> clickable();

  /**
   * Show the reward's description.
   */
  Observable<String> description();

  /**
   * Show the estimated delivery date.
   */
  Observable<DateTime> estimatedDelivery();

  /**
   * Returns `true` if the estimated delivery section should be hidden, `false` otherwise.
   */
  Observable<Boolean> estimatedDeliveryIsHidden();

  /**
   * Start checkout with the project's reward selected.
   */
  Observable<Pair<Project, Reward>> goToCheckout();

  /**
   * Show the reward limit and number remaining.
   */
  Observable<Pair<String, String>> limitAndRemaining();

  /**
   * Returns `true` if the limit section should be hidden, `false` otherwise.
   */
  Observable<Boolean> limitIsHidden();

  /**
   * Show the reward's minimum pledge.
   */
  Observable<String> minimum();

  /**
   * Returns `true` if selected reward section should be hidden, `false` otherwise.
   */
  Observable<Boolean> selectedRewardIsHidden();

  /**
   * Show the shipping summary.
   */
  Observable<String> shippingSummary();

  /**
   * Returns `true` if the shipping section should be hidden, `false` otherwise.
   */
  Observable<Boolean> shippingSummaryIsHidden();

  /**
   * Show the USD conversion.
   */
  Observable<String> usdConversion();

  /**
   * Returns `true` if the USD conversion section should be hidden, `false` otherwise.
   */
  Observable<Boolean> usdConversionIsHidden();
}
