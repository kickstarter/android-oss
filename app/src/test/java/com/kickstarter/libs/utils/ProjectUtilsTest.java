package com.kickstarter.libs.utils;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.UserFactory;

import junit.framework.TestCase;

public final class ProjectUtilsTest extends TestCase {
  public void testIsUsUserViewingNonUsProject() {
    assertTrue(ProjectUtils.usUserViewingNonUSProject(ProjectFactory.ukProject(), UserFactory.user()));
    assertFalse(ProjectUtils.usUserViewingNonUSProject(ProjectFactory.project(), UserFactory.user()));
    assertFalse(ProjectUtils.usUserViewingNonUSProject(ProjectFactory.caProject(), UserFactory.germanUser()));
  }
}
