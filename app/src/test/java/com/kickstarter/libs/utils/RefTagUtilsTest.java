package com.kickstarter.libs.utils;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import static com.kickstarter.libs.utils.SystemUtils.secondsSinceEpoch;

public final class RefTagUtilsTest extends TestCase {

  public void testCookieNameForProject() {
    final Project project = ProjectFactory.project();

    assertEquals(RefTagUtils.cookieNameForProject(project), "ref_" + String.valueOf(project.id()));
  }

  public void testCookieValueForRefTag() {
    final RefTag refTag = RefTag.from("test");

    assertEquals(RefTagUtils.cookieValueForRefTag(refTag), "test%3F" + secondsSinceEpoch());
  }

  public void testFindRefTagCookieForProject() {
  }

  public void testStoredCookieRefTagForProject() {

  }

  public void testCookieForRefTagAndProject() {

  }
}