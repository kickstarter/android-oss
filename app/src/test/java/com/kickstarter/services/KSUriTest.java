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
  private final Uri updatesUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts");
  private final Uri updateUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id");

  @Test
  public void testKSUri_isDiscoverCategoriesPath() {
    assertTrue(KSUri.isDiscoverCategoriesPath(discoverCategoriesUri.getPath()));
    assertFalse(KSUri.isDiscoverCategoriesPath(discoverPlacesUri.getPath()));
  }

  @Test
  public void testKSUri_isDiscoverPlacesPath() {
    assertTrue(KSUri.isDiscoverPlacesPath(discoverPlacesUri.getPath()));
    assertFalse(KSUri.isDiscoverPlacesPath(discoverCategoriesUri.getPath()));
  }

  @Test
  public void testKSUri_isDiscoverScopePath() {
    assertTrue(KSUri.isDiscoverScopePath(discoverScopeUri.getPath(), "ending-soon"));
  }

  @Test
  public void testKSUri_isKickstarterUri() {
    final Uri ksrUri = Uri.parse("https://www.ksr.com/discover");
    final Uri uri = Uri.parse("https://www.hello-world.org/goodbye");

    assertTrue(KSUri.isKickstarterUri(ksrUri, webEndpoint));
    assertFalse(KSUri.isKickstarterUri(uri, webEndpoint));
  }

  @Test
  public void testKSUri_isModalUri() {
    final Uri modalUri = Uri.parse("https://www.ksr.com/project?modal=true");

    assertTrue(KSUri.isModalUri(modalUri, webEndpoint));
    assertFalse(KSUri.isModalUri(projectUri, webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdateCommentsUri() {
    final Uri updateCommentsUri = Uri.parse("https://www.ksr.com/projects/creator/project/posts/id/comments");

    assertTrue(KSUri.isProjectUpdateCommentsUri(updateCommentsUri, webEndpoint));
    assertFalse(KSUri.isProjectUpdateCommentsUri(updatesUri, webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdateUri() {
    assertTrue(KSUri.isProjectUpdateUri(updateUri, webEndpoint));
    assertFalse(KSUri.isProjectUpdateUri(updatesUri, webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUpdatesUri() {
    assertTrue(KSUri.isProjectUpdatesUri(updatesUri, webEndpoint));
    assertFalse(KSUri.isProjectUpdatesUri(updateUri, webEndpoint));
  }

  @Test
  public void testKSUri_isProjectUri() {
    assertTrue(KSUri.isProjectUri(projectUri, webEndpoint));
    assertFalse(KSUri.isProjectUri(updateUri, webEndpoint));
  }
}
