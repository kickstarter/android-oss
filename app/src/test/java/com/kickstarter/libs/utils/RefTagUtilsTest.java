package com.kickstarter.libs.utils;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;

public final class RefTagUtilsTest extends TestCase {

  public void testCookieNameForProject() {
    final Project project = ProjectFactory.project();

    assertEquals("ref_" + String.valueOf(project.id()), RefTagUtils.cookieNameForProject(project));
  }

  public void testCookieValueForRefTag() {
    final RefTag refTag = RefTag.from("test");

    assertEquals("test%3F" + SystemUtils.secondsSinceEpoch(), RefTagUtils.cookieValueForRefTag(refTag));
  }

  public void testStoredCookieRefTagForProject() {
    final CookieManager cookieManager = new CookieManager();
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final Project project = ProjectFactory.project();
    final RefTag refTag = RefTag.recommended();

    // set the cookie and retrieve the ref tag
    cookieStore.add(null, new HttpCookie("ref_" + project.id(), refTag.tag() + "%3F" + SystemUtils.secondsSinceEpoch()));
    final RefTag retrievedRefTag = RefTagUtils.storedCookieRefTagForProject(project, cookieManager);

    assertNotNull(retrievedRefTag);
    assertEquals(refTag, retrievedRefTag);
  }

  public void testBuildCookieForRefTagAndProject_WithWellFormedUrl() {
    final Project project = ProjectFactory.project();
    final RefTag refTag = RefTag.category();
    final HttpCookie cookie = RefTagUtils.buildCookieForRefTagAndProject(refTag, project);

    assertNotNull(cookie);
    assertEquals(ProjectUtils.timeInSecondsUntilDeadline(project).longValue(), cookie.getMaxAge());
    assertEquals("www.kickstarter.com", cookie.getDomain());
  }

  public void testBuildCookieForRefTagAndProject_WithMalformedUrl() {
    final Project.Urls.Web webUrls = ProjectFactory.project().urls().web().toBuilder().project("such:\\bad^<data").build();
    final Project.Urls urls = ProjectFactory.project().urls().toBuilder().web(webUrls).build();
    final Project project = ProjectFactory.project().toBuilder().urls(urls).build();

    final RefTag refTag = RefTag.category();
    final HttpCookie cookie = RefTagUtils.buildCookieForRefTagAndProject(refTag, project);

    assertNull(cookie);
  }

  public void testFindRefTagCookieForProject_WhenCookieExists() {
    final CookieManager cookieManager = new CookieManager();
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final Project project = ProjectFactory.project();
    final RefTag refTag = RefTag.recommended();

    // set and retrieve the cookie
    cookieStore.add(null, new HttpCookie("ref_" + project.id(), refTag.tag() + "%3F" + SystemUtils.secondsSinceEpoch()));
    final HttpCookie cookie = RefTagUtils.findRefTagCookieForProject(project, cookieManager);

    assertNotNull(cookie);
    assertEquals(RefTagUtils.cookieNameForProject(project), cookie.getName());
    assertEquals(RefTagUtils.cookieValueForRefTag(refTag), cookie.getValue());
  }

  public void testFindRefTagCookieForProject_WhenCookieDoesNotExist() {
    final CookieManager cookieManager = new CookieManager();
    final CookieStore cookieStore = cookieManager.getCookieStore();
    final Project project = ProjectFactory.project();
    final RefTag refTag = RefTag.recommended();

    // retrieve the cookie
    final HttpCookie cookie = RefTagUtils.findRefTagCookieForProject(project, cookieManager);

    assertNull(cookie);
  }
}
