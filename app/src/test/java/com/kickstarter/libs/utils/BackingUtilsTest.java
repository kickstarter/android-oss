package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.BackingFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;

import org.junit.Test;

public final class BackingUtilsTest extends KSRobolectricTestCase {

  @Test
  public void testIsBacked() {
    final Project backedProject = ProjectFactory.backedProject();
    assertTrue(BackingUtils.isBacked(backedProject, backedProject.backing().reward()));
    assertFalse(BackingUtils.isBacked(backedProject, RewardFactory.reward()));
    assertFalse(BackingUtils.isBacked(backedProject, RewardFactory.noReward()));
    final Project noRewardBackedProject = ProjectFactory.backedProject().toBuilder()
      .backing(BackingFactory.backing(backedProject, UserFactory.user(), RewardFactory.noReward()))
      .build();
    assertTrue(BackingUtils.isBacked(noRewardBackedProject, RewardFactory.noReward()));
  }

  @Test
  public void testIsErrored() {
    assertFalse(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_CANCELED)));
    assertFalse(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_COLLECTED)));
    assertFalse(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_DROPPED)));
    assertTrue(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_ERRORED)));
    assertFalse(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_PLEDGED)));
    assertFalse(BackingUtils.isErrored(BackingFactory.backing(Backing.STATUS_PREAUTH)));
  }

  @Test
  public void testIsShippable() {
    final Backing backingWithShipping = BackingFactory.backing().toBuilder()
      .reward(RewardFactory.rewardWithShipping())
      .build();

    assertTrue(BackingUtils.isShippable(backingWithShipping));
    assertFalse(BackingUtils.isShippable(BackingFactory.backing()));
  }
}
