package com.kickstarter.libs.utils;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;

import junit.framework.TestCase;

public final class ProjectUtilsTest extends TestCase {
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
}
