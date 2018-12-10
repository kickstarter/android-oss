package com.kickstarter.libs.utils;

import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.models.Reward;

import junit.framework.TestCase;

public final class RewardUtilsTest extends TestCase {

  public void testHasBackers() {
    assertTrue(RewardUtils.hasBackers(RewardFactory.backers()));
    assertFalse(RewardUtils.hasBackers(RewardFactory.noBackers()));
  }

  public void testIsLimited() {
    final Reward rewardWithRemaining = RewardFactory.reward().toBuilder()
      .remaining(5)
      .limit(10)
      .build();
    assertTrue(RewardUtils.isLimited(rewardWithRemaining));

    final Reward rewardWithNoneRemaining = RewardFactory.reward().toBuilder()
      .remaining(0)
      .limit(10)
      .build();
    assertFalse(RewardUtils.isLimited(rewardWithNoneRemaining));

    final Reward rewardWithNoLimitAndRemainingSet = RewardFactory.reward().toBuilder()
      .remaining(null)
      .limit(null)
      .build();
    assertFalse(RewardUtils.isLimited(rewardWithNoLimitAndRemainingSet));
  }

  public void testIsItemized() {
    assertFalse(RewardUtils.isItemized(RewardFactory.reward()));
    assertTrue(RewardUtils.isItemized(RewardFactory.itemized()));
  }

  public void testIsLimitReachedWhenLimitSetAndRemainingIsZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(0)
      .build();
    assertTrue(RewardUtils.isLimitReached(reward));
  }

  public void testIsLimitNotReachedWhenLimitSetButRemainingIsNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .build();
    assertFalse(RewardUtils.isLimitReached(reward));
  }

  public void testIsLimitReachedWhenRemainingIsGreaterThanZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(50)
      .build();
    assertFalse(RewardUtils.isLimitReached(reward));
  }

  public void testIsReward() {
    assertTrue(RewardUtils.isReward(RewardFactory.reward()));
    assertFalse(RewardUtils.isReward(RewardFactory.noReward()));
  }

  public void testIsNoReward() {
    assertTrue(RewardUtils.isNoReward(RewardFactory.noReward()));
    assertFalse(RewardUtils.isNoReward(RewardFactory.reward()));
  }

  public void testIsShippable() {
    final Reward rewardWithNullShippingEnabled = RewardFactory.reward().toBuilder()
      .shippingEnabled(null)
      .build();
    assertFalse(RewardUtils.isShippable(rewardWithNullShippingEnabled));

    final Reward rewardWithFalseShippingEnabled = RewardFactory.reward().toBuilder()
      .shippingEnabled(false)
      .build();
    assertFalse(RewardUtils.isShippable(rewardWithFalseShippingEnabled));

    final Reward rewardWithShippingEnabled = RewardFactory.reward().toBuilder()
      .shippingEnabled(true)
      .build();
    assertTrue(RewardUtils.isShippable(rewardWithShippingEnabled));
  }
}
