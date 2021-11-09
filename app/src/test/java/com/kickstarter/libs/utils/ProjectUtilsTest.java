package com.kickstarter.libs.utils;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.utils.extensions.ProjectExt;
import com.kickstarter.libs.utils.extensions.ProjectMetadata;
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
    assertEquals(ProjectExt.combineProjectsAndParams(Collections.singletonList(project), discoveryParams),
      Collections.singletonList(Pair.create(project, discoveryParams)));
  }

  @Test
  public void testIsCompleted() {
    assertTrue(ProjectExt.isCompleted(ProjectFactory.successfulProject()));
    assertFalse(ProjectExt.isCompleted(ProjectFactory.project()));
  }

  @Test
  public void testIsUsUserViewingNonUsProject() {
    assertTrue(ProjectExt.isUSUserViewingNonUSProject(
      UserFactory.user().location().country(),
      ProjectFactory.ukProject().country())
    );
    assertFalse(ProjectExt.isUSUserViewingNonUSProject(
      UserFactory.user().location().country(),
      ProjectFactory.project().country())
    );
    assertFalse(ProjectExt.isUSUserViewingNonUSProject(
      UserFactory.germanUser().location().country(),
      ProjectFactory.caProject().country())
    );
  }

  @Test
  public void testMetadataForProject() {
    assertEquals(null, ProjectExt.metadataForProject(ProjectFactory.project()));
    assertEquals(ProjectMetadata.BACKING, ProjectExt.metadataForProject(ProjectFactory.backedProject()));
    assertEquals(ProjectMetadata.CATEGORY_FEATURED, ProjectExt.metadataForProject(ProjectFactory.featured()));
    assertEquals(ProjectMetadata.SAVING, ProjectExt.metadataForProject(ProjectFactory.saved()));
    final Project savedAndBacked = ProjectFactory.backedProject().toBuilder().isStarred(true).build();
    assertEquals(ProjectMetadata.BACKING, ProjectExt.metadataForProject(savedAndBacked));
    final DateTime now = new DateTime();
    final Project savedAndFeatured = ProjectFactory.saved().toBuilder().featuredAt(now).build();
    assertEquals(ProjectMetadata.SAVING, ProjectExt.metadataForProject(savedAndFeatured));
    final Project savedBackedFeatured = ProjectFactory.backedProject().toBuilder().featuredAt(now).isStarred(true).build();
    assertEquals(ProjectMetadata.BACKING, ProjectExt.metadataForProject(savedBackedFeatured));
  }

  @Test
  public void testPhotoHeightFromWidthRatio() {
    assertEquals(360, ProjectExt.photoHeightFromWidthRatio(640));
    assertEquals(576, ProjectExt.photoHeightFromWidthRatio(1024));
  }
}
