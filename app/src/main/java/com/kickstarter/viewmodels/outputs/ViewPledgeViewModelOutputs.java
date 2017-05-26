package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.models.RewardsItem;

import java.util.List;

import rx.Observable;

public interface ViewPledgeViewModelOutputs {
  /**
   * Set the backer name TextView's text.
   */
  Observable<String> backerNameTextViewText();

  /**
   * Set the backer number TextView's text.
   */
  Observable<String> backerNumberTextViewText();

  /**
   * Set the backing status TextView's text.
   */
  Observable<String> backingStatus();

  /**
   * Set the backing amount and date TextView's text.
   */
  Observable<Pair<String, String>> backingAmountAndDateTextViewText();

  /*
   * Set the creator name TextView's text.
   */
  Observable<String> creatorNameTextViewText();

  /*
   *  Whether to hide the estimated delivery date section
   */
  Observable<Boolean> estimatedDeliverySectionIsGone();

  /**
   * Navigate back.
   */
  Observable<Void> goBack();

  /**
   * Load the backer avatar given the URL.
   */
  Observable<String> loadBackerAvatar();

  /**
   * Load the project photo given the URL.
   */
  Observable<String> loadProjectPhoto();

  /*
   * Set the project name TextView's text.
   */
  Observable<String> projectNameTextViewText();

  /**
   * Set the reward minimum and description TextView's text.
   */
  Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText();

  /**
   * Show the rewards items.
   */
  Observable<List<RewardsItem>> rewardsItems();

  /**
   * Returns `true` if the items section should be hidden, `false` otherwise.
   */
  Observable<Boolean> rewardsItemsAreHidden();

  /**
   * Set the shipping amount TextView's text.
   */
  Observable<String> shippingAmountTextViewText();

  /**
   * Set the shipping location TextView's text.
   */
  Observable<String> shippingLocationTextViewText();

  /**
   * Set the visibility of the shipping section.
   */
  Observable<Boolean> shippingSectionIsHidden();
}
