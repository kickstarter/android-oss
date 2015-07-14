package com.kickstarter;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(KsrRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ProjectTest extends TestCase {
  Project projectWithSecureUrl() {
    Project project = ProjectFactory.project();
    project.urls.web.project = "https://www.kickstarter.com/projects/foo/bar";

    return project;
  }
  @Test
  public void testSecureWebProjectUrl() {
    Project project = ProjectFactory.project();
    project.urls.web.project = "http://www.kickstarter.com/projects/foo/bar";

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
}
