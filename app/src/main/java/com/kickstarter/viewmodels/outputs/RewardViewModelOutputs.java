package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;

import org.joda.time.DateTime;

import java.util.List;

import rx.Observable;

public interface RewardViewModelOutputs {
  /**
   * Returns `true` if the all gone header should be hidden, `false` otherwise.
   */
  Observable<Boolean> allGoneHeaderIsHidden();

  /**
   * Set the backers TextView's text.
   */
  Observable<Integer> backersTextViewText();

  /**
   * Returns `true` if the number of backers TextView should be hidden, `false` otherwise.
   */
  Observable<Boolean> backersTextViewIsHidden();

  /**
   * Set the description TextView's text.
   */
  Observable<String> descriptionTextViewText();

  /**
   * Set the estimated delivery date TextView's text.
   */
  Observable<DateTime> estimatedDeliveryDateTextViewText();

  /**
   * Returns `true` if the estimated delivery section should be hidden, `false` otherwise.
   */
  Observable<Boolean> estimatedDeliveryDateSectionIsHidden();

  /**
   * Start checkout with the project's reward selected.
   */
  Observable<Pair<Project, Reward>> goToCheckout();

  /**
   * Start checkout with the project's reward selected.
   */
  Observable<Project> goToViewPledge();

  /**
   * Returns `true` if reward can be clicked, `false` otherwise.
   */
  Observable<Boolean> isClickable();

  /**
   * Returns `true` if the limit and remaining section should be center-aligned, `false` otherwise.
   */
  Observable<Boolean> limitAndRemainingSectionIsCenterAligned();

  /**
   * Returns `true` if the limit quantity section should be hidden, `false` otherwise.
   */
  Observable<Boolean> limitAndRemainingSectionIsHidden();

  /**
   * Set the limit and remaining TextView's text.
   */
  Observable<Pair<String, String>> limitAndRemainingTextViewText();

  /**
   * Returns `true` if the divider between the time limit and the quantity limit should be hidden, `false` otherwise.
   */
  Observable<Boolean> limitDividerIsHidden();

  /**
   * Returns `true` if the limit header should be hidden, `false` otherwise.
   */
  Observable<Boolean> limitHeaderIsHidden();

  /**
   * Set the minimum button's text.
   */
  Observable<String> minimumButtonText();

  /**
   * Returns `true` if the minimum button should be hidden, `false` otherwise.
   */
  Observable<Boolean> minimumButtonIsHidden();

  /**
   * Set the minimum TextView's text.
   */
  Observable<String> minimumTextViewText();

  /**
   * Returns `true` if the minimum text view should be hidden, `false` otherwise.
   */
  Observable<Boolean> minimumTextViewIsHidden();

  /**
   * Set the title's text using the minimum pledge.
   */
  Observable<String> minimumTitleTextViewText();

  /**
   * Show the rewards items.
   */
  Observable<List<RewardsItem>> rewardsItems();

  /**
   * Returns `true` if the items section should be hidden, `false` otherwise.
   */
  Observable<Boolean> rewardsItemsAreHidden();

  /**
   * Use the reward's title to set the title text.
   */
  Observable<String> rewardTitleTextViewText();

  /**
   * Returns `true` if selected header should be hidden, `false` otherwise.
   */
  Observable<Boolean> selectedHeaderIsHidden();

  /**
   * Returns `true` if selected overlay should be hidden, `false` otherwise.
   */
  Observable<Boolean> selectedOverlayIsHidden();

  /**
   * Returns `true` if the shipping section should be hidden, `false` otherwise.
   */
  Observable<Boolean> shippingSummarySectionIsHidden();

  /**
   * Set the shipping summary TextView's text.
   */
  Observable<String> shippingSummaryTextViewText();

  /**
   * Returns `true` if the time limit should be hidden, `false` otherwise.
   */
  Observable<Boolean> timeLimitSectionIsHidden();

  /**
   * Set the time limit.
   */
  Observable<String> timeLimitTextViewText();

  /**
   * Returns `true` if the time limit should be center-aligned, `false` otherwise.
   */
  Observable<Boolean> timeLimitSectionIsCenterAligned();

  /**
   * Returns `true` if the USD conversion section should be hidden, `false` otherwise.
   */
  Observable<Boolean> usdConversionTextViewIsHidden();

  /**
   * Set the USD conversion.
   */
  Observable<String> usdConversionTextViewText();

  /**
   * Returns `true` if the white overlay indicating a reward is disabled should be hidden, `false` otherwise.
   */
  Observable<Boolean> whiteOverlayIsHidden();
}
