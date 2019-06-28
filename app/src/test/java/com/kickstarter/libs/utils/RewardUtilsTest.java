package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;
import org.junit.Test;

public final class RewardUtilsTest extends KSRobolectricTestCase {

  @Test
  public void testIsAvailable() {
    assertTrue(RewardUtils.isAvailable(ProjectFactory.project(), RewardFactory.reward()));
    assertFalse(RewardUtils.isAvailable(ProjectFactory.project(), RewardFactory.ended()));
    assertFalse(RewardUtils.isAvailable(ProjectFactory.project(), RewardFactory.limitReached()));
    assertFalse(RewardUtils.isAvailable(ProjectFactory.successfulProject(), RewardFactory.reward()));
    assertFalse(RewardUtils.isAvailable(ProjectFactory.successfulProject(), RewardFactory.ended()));
    assertFalse(RewardUtils.isAvailable(ProjectFactory.successfulProject(), RewardFactory.limitReached()));
  }

  @Test
  public void testHasBackers() {
    assertTrue(RewardUtils.hasBackers(RewardFactory.backers()));
    assertFalse(RewardUtils.hasBackers(RewardFactory.noBackers()));
  }

  @Test
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

  @Test
  public void testIsItemized() {
    assertFalse(RewardUtils.isItemized(RewardFactory.reward()));
    assertTrue(RewardUtils.isItemized(RewardFactory.itemized()));
  }

  @Test
  public void testIsLimitReachedWhenLimitSetAndRemainingIsZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(0)
      .build();
    assertTrue(RewardUtils.isLimitReached(reward));
  }

  @Test
  public void testIsLimitNotReachedWhenLimitSetButRemainingIsNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .build();
    assertFalse(RewardUtils.isLimitReached(reward));
  }

  @Test
  public void testIsLimitReachedWhenRemainingIsGreaterThanZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(50)
      .build();
    assertFalse(RewardUtils.isLimitReached(reward));
  }

  @Test
  public void testIsMaxRewardAmount() {
    assertTrue(RewardUtils.isMaxRewardAmount(RewardFactory.maxReward().minimum()));
    assertFalse(RewardUtils.isMaxRewardAmount(RewardFactory.noReward().minimum()));
  }

  @Test
  public void testIsReward() {
    assertTrue(RewardUtils.isReward(RewardFactory.reward()));
    assertFalse(RewardUtils.isReward(RewardFactory.noReward()));
  }

  @Test
  public void testIsNoReward() {
    assertTrue(RewardUtils.isNoReward(RewardFactory.noReward()));
    assertFalse(RewardUtils.isNoReward(RewardFactory.reward()));
  }

  @Test
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

  @Test
  public void isTimeLimited() {
    assertFalse(RewardUtils.isTimeLimited(RewardFactory.reward()));
    assertTrue(RewardUtils.isTimeLimited(RewardFactory.endingSoon()));
  }

  @Test
  public void testIsExpired() {
    assertFalse(RewardUtils.isExpired(RewardFactory.reward()));
    final Reward rewardEnded2DaysAgo = RewardFactory.reward()
      .toBuilder()
      .endsAt(DateTime.now().minusDays(2))
      .build();
    assertTrue(RewardUtils.isExpired(rewardEnded2DaysAgo));
    final Reward rewardEndingIn2Days = RewardFactory.reward()
      .toBuilder()
      .endsAt(DateTime.now().plusDays(2))
      .build();
    assertFalse(RewardUtils.isExpired(rewardEndingIn2Days));
  }
}
