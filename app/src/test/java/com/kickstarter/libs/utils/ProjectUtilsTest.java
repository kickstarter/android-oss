package com.kickstarter.libs.utils;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Project;

import org.joda.time.DateTime;
import org.junit.Test;

public final class ProjectUtilsTest extends KSRobolectricTestCase {
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
}
