package com.kickstarter.models;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;

import org.joda.time.DateTime;

import org.junit.Test;

public class ProjectTest extends KSRobolectricTestCase {
  Project projectWithSecureUrl() {
    final String projectUrl = "https://www.kickstarter.com/projects/foo/bar";
    final Project.Urls urls = Project.Urls.builder()
      .web(Project.Urls.Web.builder().project(projectUrl).rewards(projectUrl + "/rewards").build())
      .build();

    return ProjectFactory.project().toBuilder().urls(urls).build();
  }

  @Test
  public void testSecureWebProjectUrl() {
    final String projectUrl = "http://www.kickstarter.com/projects/foo/bar";

    final Project.Urls urls = Project.Urls.builder()
      .web(Project.Urls.Web.builder().project(projectUrl).rewards(projectUrl + "/rewards").build())
      .build();

    final Project project = ProjectFactory.project().toBuilder().urls(urls).build();

    assertEquals("https://www.kickstarter.com/projects/foo/bar", project.secureWebProjectUrl());
  }

  @Test
  public void testNewPledgeUrl() {
    assertEquals("https://www.kickstarter.com/projects/foo/bar/pledge/new", projectWithSecureUrl().newPledgeUrl());
  }

  @Test
  public void testEditPledgeUrl() {
    assertEquals("https://www.kickstarter.com/projects/foo/bar/pledge/edit", projectWithSecureUrl().editPledgeUrl());
  }

  @Test
  public void testPercentageFunded() {
    assertEquals(50.0f, ProjectFactory.halfWayProject().percentageFunded());
    assertEquals(100.0f, ProjectFactory.allTheWayProject().percentageFunded());
    assertEquals(200.0f, ProjectFactory.doubledGoalProject().percentageFunded());
  }

  @Test
  public void testIsApproachingDeadline() {
    final Project projectApproachingDeadline = ProjectFactory.project().toBuilder()
      .deadline(new DateTime().plusDays(1)).build();
    final Project projectNotApproachingDeadline = ProjectFactory.project().toBuilder()
      .deadline(new DateTime().plusDays(3)).build();

    assertTrue(projectApproachingDeadline.isApproachingDeadline());
    assertFalse(projectNotApproachingDeadline.isApproachingDeadline());
  }
}
