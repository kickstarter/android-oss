package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.RewardFactory;
import com.kickstarter.models.Project;

import org.junit.Test;

public final class BackingUtilsTest extends KSRobolectricTestCase {

  @Test
  public void testIsBacked() {
    final Project backedProject = ProjectFactory.backedProject();
    assertTrue(BackingUtils.isBacked(backedProject, backedProject.backing().reward()));
    assertFalse(BackingUtils.isBacked(backedProject, RewardFactory.reward()));
  }
}
