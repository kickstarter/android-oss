package com.kickstarter.libs.utils;

import android.content.Context;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.utils.extensions.ProjectExt;
import com.kickstarter.libs.utils.extensions.ProjectMetadata;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.Mockito;

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
  public void testing123(){
    final Context context = Mockito.mock(Context.class);
    Mockito.when(context.getString(R.string.discovery_baseball_card_deadline_units_secs)).thenReturn("secs");
        Mockito.when(context.getString(R.string.discovery_baseball_card_deadline_units_mins)).thenReturn("mins");
        Mockito.when(context.getString(R.string.discovery_baseball_card_deadline_units_hours)).thenReturn("hours");
        Mockito.when(context.getString(R.string.discovery_baseball_card_deadline_units_days)).thenReturn("days");

        Project project = ProjectFactory.project().toBuilder().deadline(new DateTime((DateTime.now().plusDays(1)))).build();
//    assertEquals("hours", ProjectUtils.deadlineCountdownUnit(project, context));
//
//    project = ProjectFactory.project().toBuilder().deadline(new DateTime((DateTime.now().plusMinutes(10)))).build();
//    assertEquals("mins", ProjectUtils.deadlineCountdownUnit(project, context));
//
//    project = ProjectFactory.project().toBuilder().deadline(new DateTime((DateTime.now().plusSeconds(25)))).build();
//    assertEquals("secs", ProjectUtils.deadlineCountdownUnit(project, context));
//
//    project = ProjectFactory.project().toBuilder().deadline(new DateTime((DateTime.now().plusDays(10)))).build();
//    assertEquals("days", ProjectUtils.deadlineCountdownUnit(project, context));

    project = ProjectFactory.project().toBuilder().deadline(new DateTime(DateTime.now().plusDays(2))).build();

    assertEquals("47 hours", ProjectUtils.deadlineCountdown(project, context));
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
