package com.kickstarter;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class ProjectTest extends TestCase {
  Project projectWithSecureUrl() {
    final Project.Urls urls = Project.Urls.builder()
      .web(Project.Urls.Web.builder().project("https://www.kickstarter.com/projects/foo/bar").build())
      .build();

    return ProjectFactory.project().urls(urls).build();
  }

  @Test
  public void testSecureWebProjectUrl() {
    final Project.Urls urls = Project.Urls.builder()
      .web(Project.Urls.Web.builder().project("https://www.kickstarter.com/projects/foo/bar").build())
      .build();

    final Project project = ProjectFactory.project().urls(urls).build();

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
