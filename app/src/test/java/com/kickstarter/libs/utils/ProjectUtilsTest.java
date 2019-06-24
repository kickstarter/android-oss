package com.kickstarter.libs.utils;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Collections;

public final class ProjectUtilsTest extends KSRobolectricTestCase {

  @Test
  public void testCombineProjectsAndParams() {
    final Project project = ProjectFactory.project();
    final DiscoveryParams discoveryParams = DiscoveryParams.builder().build();
    assertEquals(ProjectUtils.combineProjectsAndParams(Collections.singletonList(project), discoveryParams),
      Collections.singletonList(Pair.create(project, discoveryParams)));
  }

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
  public void testMetadataForProject() {
    assertEquals(null, ProjectUtils.metadataForProject(ProjectFactory.project()));
    assertEquals(ProjectUtils.Metadata.BACKING, ProjectUtils.metadataForProject(ProjectFactory.backedProject()));
    assertEquals(ProjectUtils.Metadata.CATEGORY_FEATURED, ProjectUtils.metadataForProject(ProjectFactory.featured()));
    assertEquals(ProjectUtils.Metadata.SAVING, ProjectUtils.metadataForProject(ProjectFactory.saved()));
    final Project savedAndBacked = ProjectFactory.backedProject().toBuilder().isStarred(true).build();
    assertEquals(ProjectUtils.Metadata.BACKING, ProjectUtils.metadataForProject(savedAndBacked));
    final DateTime now = new DateTime();
    final Project savedAndFeatured = ProjectFactory.saved().toBuilder().featuredAt(now).build();
    assertEquals(ProjectUtils.Metadata.SAVING, ProjectUtils.metadataForProject(savedAndFeatured));
    final Project savedBackedFeatured = ProjectFactory.backedProject().toBuilder().featuredAt(now).isStarred(true).build();
    assertEquals(ProjectUtils.Metadata.BACKING, ProjectUtils.metadataForProject(savedBackedFeatured));
  }

  @Test
  public void testPhotoHeightFromWidthRatio() {
    assertEquals(360, ProjectUtils.photoHeightFromWidthRatio(640));
    assertEquals(576, ProjectUtils.photoHeightFromWidthRatio(1024));
  }

  @Test
  public void testPledgeButtonColor() {
    assertEquals(R.color.button_pledge_live, ProjectUtils.pledgeButtonColor(ProjectFactory.project()));
    assertEquals(R.color.button_pledge_manage, ProjectUtils.pledgeButtonColor(ProjectFactory.backedProject()));
    assertEquals(R.color.button_pledge_live, ProjectUtils.pledgeButtonColor(ProjectFactory.successfulProject()));
    final Project backedSuccessfulProject = ProjectFactory.backedProject()
      .toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    assertEquals(R.color.button_pledge_ended, ProjectUtils.pledgeButtonColor(backedSuccessfulProject));
  }
}
