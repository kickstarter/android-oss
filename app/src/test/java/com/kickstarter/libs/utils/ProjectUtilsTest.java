package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;

import org.junit.Test;

public final class ProjectUtilsTest extends KSRobolectricTestCase {
  @Test
  public void testIsCompleted() {
    assertTrue(ProjectUtils.isCompleted(ProjectFactory.successfulProject()));
    assertFalse(ProjectUtils.isCompleted(ProjectFactory.project()));
  }

  @Test
  public void testIsUsUserViewingNonUsProject() {
    assertTrue(ProjectUtils.isUSUserViewingNonUSProject(
      UserFactory.user().location().country(),
      ProjectFactory.ukProject().country())
    );
    assertFalse(ProjectUtils.isUSUserViewingNonUSProject(
      UserFactory.user().location().country(),
      ProjectFactory.project().country())
    );
    assertFalse(ProjectUtils.isUSUserViewingNonUSProject(
      UserFactory.germanUser().location().country(),
      ProjectFactory.caProject().country())
    );
  }

  @Test
  public void testPhotoHeightFromWidthRatio() {
    assertEquals(360, ProjectUtils.photoHeightFromWidthRatio(640));
    assertEquals(576, ProjectUtils.photoHeightFromWidthRatio(1024));
  }
}
