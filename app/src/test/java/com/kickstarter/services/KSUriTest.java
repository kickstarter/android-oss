package com.kickstarter.services;

import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

public final class KSUriTest extends KSRobolectricTestCase {
  private final Uri discoverCategoriesUri = Uri.parse("https://www.ksr.com/discover/categories/art");
  private final Uri discoverScopeUri = Uri.parse("https://www.kickstarter.com/discover/ending-soon");
  private final Uri discoverPlacesUri = Uri.parse("https://www.ksr.com/discover/places/newest");
  private final Uri newGuestCheckoutUri = Uri.parse("https://www.ksr.com/checkouts/1/guest/new");
  private final Uri projectUri = Uri.parse("https://www.ksr.com/projects/creator/project");
  private final Uri projectPreviewUri = Uri.parse("https://www.ksr.com/projects/creator/project?token=token");
  private final Uri projectSurveyUri = Uri.parse("https://www.ksr.com/projects/creator/project/surveys/survey-param");
  private final Uri updatesUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts");
  private final Uri updateUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id");
  private final Uri userSurveyUri = Uri.parse("https://www.ksr.com/users/user-param/surveys/survey-id");
  private final String webEndpoint = "https://www.ksr.com";

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
  public void testKSUri_isWebViewUri() {
    final Uri ksrUri = Uri.parse("https://www.ksr.com/project");
    final Uri uri = Uri.parse("https://www.hello-world.org/goodbye");
    final Uri ksrGraphUri = Uri.parse("https://www.ksr.com/graph");
    final Uri graphUri = Uri.parse("https://www.hello-world.org/graph");
    final Uri favIconUri = Uri.parse("https://www.ksr.com/favicon.ico");

    assertTrue(KSUri.isWebViewUri(ksrUri, this.webEndpoint));
    assertFalse(KSUri.isWebViewUri(uri, this.webEndpoint));
    assertFalse(KSUri.isWebViewUri(ksrGraphUri, this.webEndpoint));
    assertFalse(KSUri.isWebViewUri(graphUri, this.webEndpoint));
    assertFalse(KSUri.isWebViewUri(favIconUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isKSFavIcon() {
    final Uri ksrUri = Uri.parse("https://www.ksr.com/favicon.ico");
    final Uri uri = Uri.parse("https://www.hello-world.org/goodbye");

    assertTrue(KSUri.isKSFavIcon(ksrUri, this.webEndpoint));
    assertFalse(KSUri.isKSFavIcon(uri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isKSGraphQLUri() {
    final Uri ksrGraphUri = Uri.parse("https://www.ksr.com/graph");
    final Uri graphUri = Uri.parse("https://www.hello-world.org/graph");

    assertTrue(KSUri.isKSGraphQLUri(ksrGraphUri, this.webEndpoint));
    assertFalse(KSUri.isKSGraphQLUri(graphUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isModalUri() {
    final Uri modalUri = Uri.parse("https://www.ksr.com/project?modal=true");

    assertTrue(KSUri.isModalUri(modalUri, this.webEndpoint));
    assertFalse(KSUri.isModalUri(this.projectUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isNewGuestCheckoutUri() {
    assertTrue(KSUri.isNewGuestCheckoutUri(this.newGuestCheckoutUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectSurveyUri() {
    assertTrue(KSUri.isProjectSurveyUri(this.projectSurveyUri, this.webEndpoint));
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
    assertTrue(KSUri.isProjectUri(this.projectPreviewUri, this.webEndpoint));
    assertFalse(KSUri.isProjectUri(this.updateUri, this.webEndpoint));
  }

  @Test
  public void testKSUri_isProjectPreviewUri() {
    assertTrue(KSUri.isProjectPreviewUri(this.projectPreviewUri, this.webEndpoint));
    assertFalse(KSUri.isProjectPreviewUri(this.projectUri, this.webEndpoint));
  }

  @Test
  public void testKSuri_isUserSurveyUri() {
    assertTrue(KSUri.isUserSurveyUri(this.userSurveyUri, this.webEndpoint));
    assertFalse(KSUri.isUserSurveyUri(this.projectSurveyUri, this.webEndpoint));
  }
}
