package com.kickstarter.libs.utils;

import android.content.Context;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.models.OptimizelyExperiment;
import com.kickstarter.mock.factories.LocationFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.junit.Test;

import java.util.Date;

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
  public void testDeadlineCountdownDetailWithDaysLeft() {
    final Context context = context();
    final KSString ksString = ksString();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addDays(31);

    final Reward rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "days to go");
  }

  @Test
  public void testDeadlineCountdownDetailWithHoursLeft() {
    final Context context = context();
    final KSString ksString = ksString();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addHours(3);

    final Reward rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "hours to go");
  }

  @Test
  public void testDeadlineCountdownDetailWithSecondsLeft() {
    final Context context = context();
    final KSString ksString = ksString();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(3);

    final Reward rewardWith30DaysRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownDetail(rewardWith30DaysRemaining, context, ksString), "secs to go");
  }

  @Test
  public void testDeadlineCountdownUnitWithDaysLeft() {
    final Context context = context();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addDays(31);

    final Reward rewardWithDaysRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownUnit(rewardWithDaysRemaining, context), "days");
  }

  @Test
  public void testDeadlineCountdownUnitWithHoursLeft() {
    final Context context = context();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addHours(3);

    final Reward rewardWithHoursRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownUnit(rewardWithHoursRemaining, context), "hours");
  }

  @Test
  public void testDeadlineCountdownUnitWithMinutesLeft() {
    final Context context = context();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addMinutes(3);

    final Reward rewardWithMinutesRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownUnit(rewardWithMinutesRemaining, context), "mins");
  }

  @Test
  public void testDeadlineCountdownUnitWithSecondsLeft() {
    final Context context = context();
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(30);

    final Reward rewardWithSecondsRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownUnit(rewardWithSecondsRemaining, context), "secs");
  }

  @Test
  public void testDeadlineCountdownValueWithMinutesLeft() {
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(300);

    final Reward rewardWithMinutesRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownValue(rewardWithMinutesRemaining), 5);
  }

  @Test
  public void testDeadlineCountdownValueWithHoursLeft() {
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(3600);

    final Reward rewardWithHoursRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownValue(rewardWithHoursRemaining), 60);
  }

  @Test
  public void testDeadlineCountdownValueWithDaysLeft() {
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(86400);

    final Reward rewardWithDaysRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownValue(rewardWithDaysRemaining), 24);
  }


  @Test
  public void testDeadlineCountdownValueWithSecondsLeft() {
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(30);

    final Reward rewardWithSecondsRemaining = RewardFactory.reward().toBuilder()
      .endsAt(currentDate.toDateTime())
      .build();
    assertEquals(RewardUtils.deadlineCountdownValue(rewardWithSecondsRemaining), 30);
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
    final Reward rewardWithNullShipping = RewardFactory.reward()
      .toBuilder()
      .shippingType(null)
      .build();
    assertFalse(RewardUtils.isShippable(rewardWithNullShipping));

    final Reward rewardWithNoShipping = RewardFactory.reward();
    assertFalse(RewardUtils.isShippable(rewardWithNoShipping));

    final Reward rewardWithMultipleLocationShipping = RewardFactory.multipleLocationShipping();
    assertTrue(RewardUtils.isShippable(rewardWithMultipleLocationShipping));

    final Reward rewardWithSingleLocationShipping = RewardFactory.singleLocationShipping(LocationFactory.nigeria().displayableName());
    assertTrue(RewardUtils.isShippable(rewardWithSingleLocationShipping));

    final Reward rewardWithWorldWideShipping = RewardFactory.multipleLocationShipping();
    assertTrue(RewardUtils.isShippable(rewardWithWorldWideShipping));
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

  @Test
  public void testShippingSummary() {
    final Reward rewardWithNullShipping = RewardFactory.reward()
      .toBuilder()
      .shippingType(null)
      .build();
    assertNull(RewardUtils.shippingSummary(rewardWithNullShipping));

    final Reward rewardWithNoShipping = RewardFactory.reward();
    assertNull(RewardUtils.shippingSummary(rewardWithNoShipping));

    final Reward rewardWithMultipleLocationShipping = RewardFactory.multipleLocationShipping();
    assertEquals(Pair.create(R.string.Limited_shipping, null), RewardUtils.shippingSummary(rewardWithMultipleLocationShipping));

    final Reward rewardWithSingleLocationShipping = RewardFactory.singleLocationShipping(LocationFactory.nigeria().displayableName());
    assertEquals(Pair.create(R.string.location_name_only, "Nigeria"), RewardUtils.shippingSummary(rewardWithSingleLocationShipping));

    final Reward rewardWithWorldWideShipping = RewardFactory.rewardWithShipping();
    assertEquals(Pair.create(R.string.Ships_worldwide, null), RewardUtils.shippingSummary(rewardWithWorldWideShipping));
  }

  @Test
  public void minimumRewardAmountByVariant() {
    final Reward noReward = RewardFactory.noReward();
    assertEquals(1.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 1));
    assertEquals(10.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_2, noReward, 1));
    assertEquals(20.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_3, noReward, 1));
    assertEquals(50.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_4, noReward, 1));
    assertEquals(10.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 10));
    assertEquals(100.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_4, noReward, 100));
    assertEquals(100.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_3, noReward, 100));
    assertEquals(100.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_2, noReward, 100));
    assertEquals(100.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 100));
    assertEquals(50.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_4, noReward, 5));
    assertEquals(10.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_2, noReward, 5));
    assertEquals(20.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.VARIANT_3, noReward, 5));
    assertEquals(5.0, RewardUtils.rewardAmountByVariant(OptimizelyExperiment.Variant.CONTROL, noReward, 5));
  }

  @Test
  public void testTimeInSecondsUntilDeadline() {
    final Date date = DateTime.now().toDate();
    final MutableDateTime currentDate = new MutableDateTime(date);
    currentDate.addSeconds(120);
    final Reward reward = RewardFactory.reward().toBuilder().endsAt(currentDate.toDateTime()).build();
    final long timeInSecondsUntilDeadline = RewardUtils.timeInSecondsUntilDeadline(reward);
    assertEquals(timeInSecondsUntilDeadline, 120);
  }
}
