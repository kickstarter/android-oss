package com.kickstarter.libs.utils;

import com.kickstarter.factories.RewardFactory;
import com.kickstarter.models.Reward;

import junit.framework.TestCase;

public final class RewardUtilsTest extends TestCase {
  public void testLimitReachedWhenRemainingIsGreaterThanZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(50)
      .build();

    assertFalse(RewardUtils.isLimitReached(reward));
  }

  public void testLimitReachedWhenLimitSetAndRemainingIsZero() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .remaining(0)
      .build();
    assertTrue(RewardUtils.isLimitReached(reward));
  }

  public void testLimitNotReachedWhenLimitSetButRemainingIsNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .limit(100)
      .build();
    assertFalse(RewardUtils.isLimitReached(reward));
  }
}
