package com.kickstarter.libs.utils;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;

import junit.framework.TestCase;

public final class ProjectUtilsTest extends TestCase {
  public void testIsUsUserViewingNonUsProject() {
    assertTrue(ProjectUtils.isUSUserViewingNonUSProject(UserFactory.user(), ProjectFactory.ukProject()));
    assertFalse(ProjectUtils.isUSUserViewingNonUSProject(UserFactory.user(), ProjectFactory.project()));
    assertFalse(ProjectUtils.isUSUserViewingNonUSProject(UserFactory.germanUser(), ProjectFactory.caProject()));
  }
}
