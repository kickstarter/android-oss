package com.kickstarter.services;

import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

public final class KSUriTest extends KSRobolectricTestCase {
  private final Uri discoverCategoriesUri = Uri.parse("https://www.ksr.com/discover/categories/art");
  private final Uri discoverScopeUri = Uri.parse("https://www.kickstarter.com/discover/ending-soon");
  private final Uri discoverPlacesUri = Uri.parse("https://www.ksr.com/discover/places/newest");
  private final String webEndpoint = "https://www.ksr.com";
  private final Uri projectUri = Uri.parse("https://www.ksr.com/projects/creator/project");
  private final Uri projectSurveyUri = Uri.parse("https://www.ksr.com/projects/creator/project/surveys/survey-param");
  private final Uri updatesUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts");
  private final Uri updateUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id");
  private final Uri userSurveyUri = Uri.parse("https://www.ksr.com/users/user-param/surveys/survey-id");

  @Test
  public void testKSUri_isDiscoverCategoriesPath() {
    assertTrue(KSUri.isDiscoverCategoriesPath(this.discoverCategoriesUri.getPath()));
    assertFalse(KSUri.isDiscoverCategoriesPath(this.discoverPlacesUri.getPath()));
  }

  @Test
  public void testKSUri_isDiscoverPlacesPath() {
    assertTrue(KSUri.isDiscoverPlacesPath(this.discoverPlacesUri.getPath()));
    assertFalse(KSUri.isDiscoverPlacesPath(this.discoverCategoriesUri.getPath()));
  }

  @Test
  public void testKSUri_isDiscoverScopePath() {
    assertTrue(KSUri.isDiscoverScopePath(this.discoverScopeUri.getPath(), "ending-soon"));
  }

  @Test
  public void testKSUri_isKickstarterUri() {
    final Uri ksrUri = Uri.parse("https://www.ksr.com/discover");
    final Uri uri = Uri.parse("https://www.hello-world.org/goodbye");

    assertTrue(KSUri.isKickstarterUri(ksrUri, this.webEndpoint));
    assertFalse(KSUri.isKickstarterUri(uri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isModalUri() {
    final Uri modalUri = Uri.parse("https://www.ksr.com/project?modal=true");

    assertTrue(KSUri.isModalUri(modalUri, this.webEndpoint));
    assertFalse(KSUri.isModalUri(this.projectUri, this.webEndpoint));
  }

  @Test
  public void testKSuri_isProjectSurveyUri() {
    assertTrue((KSUri.isProjectSurveyUri(this.projectSurveyUri, this.webEndpoint)));
    assertFalse(KSUri.isProjectSurveyUri(this.userSurveyUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdateCommentsUri() {
    final Uri updateCommentsUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id/comments");

    assertTrue(KSUri.isProjectUpdateCommentsUri(updateCommentsUri, this.webEndpoint));
    assertFalse(KSUri.isProjectUpdateCommentsUri(this.updatesUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdateUri() {
    assertTrue(KSUri.isProjectUpdateUri(this.updateUri, this.webEndpoint));
    assertFalse(KSUri.isProjectUpdateUri(this.updatesUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdatesUri() {
    assertTrue(KSUri.isProjectUpdatesUri(this.updatesUri, this.webEndpoint));
    assertFalse(KSUri.isProjectUpdatesUri(this.updateUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUri() {
    assertTrue(KSUri.isProjectUri(this.projectUri, this.webEndpoint));
    assertFalse(KSUri.isProjectUri(this.updateUri, this.webEndpoint));
  }

  @Test
  public void testKSuri_isUserSurveyUri() {
    assertTrue((KSUri.isUserSurveyUri(this.userSurveyUri, this.webEndpoint)));
    assertFalse(KSUri.isUserSurveyUri(this.projectSurveyUri, this.webEndpoint));
  }
}
