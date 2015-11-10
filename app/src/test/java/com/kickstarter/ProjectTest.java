package com.kickstarter;

import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.models.Project;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows=ShadowMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class ProjectTest extends TestCase {
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
}
