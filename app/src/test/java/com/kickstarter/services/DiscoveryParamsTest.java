package com.kickstarter.services;

import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

public class DiscoveryParamsTest extends KSRobolectricTestCase {
  @Test
  public void testFromUri_discoverRoot() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover");
    assertEquals(DiscoveryParams.builder().build(), DiscoveryParams.fromUri(uri));
  }

  @Test
  public void testFromUri_backed() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?backed=1");
    assertEquals(DiscoveryParams.builder().backed(1).build(), DiscoveryParams.fromUri(uri));
  }

  @Test
  public void testFromUri_categories() {
    final DiscoveryParams params = DiscoveryParams.builder().categoryParam("music").build();

    final Uri categoryUri = Uri.parse("https://www.kickstarter.com/discover/categories/music");
    assertEquals(params, DiscoveryParams.fromUri(categoryUri));

    final Uri advancedUri = Uri.parse("https://www.kickstarter.com/discover/advanced?category_id=music");
    assertEquals(params, DiscoveryParams.fromUri(advancedUri));
  }

  @Test
  public void testFromUri_filters() {
    final DiscoveryParams params = DiscoveryParams.builder()
      .recommended(true)
      .social(1)
      .staffPicks(true)
      .starred(1)
      .build();

    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?recommended=true&social=1&staff_picks=true&starred=1");
    assertEquals(params, DiscoveryParams.fromUri(uri));
  }

  @Test
  public void testFromUri_locations() {
    final DiscoveryParams params = DiscoveryParams.builder().locationParam("sydney-au").build();

    final Uri placesUri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au");
    assertEquals(params, DiscoveryParams.fromUri(placesUri));

    final Uri advancedUri = Uri.parse("https://www.kickstarter.com/discover/advanced?woe_id=sydney-au");
    assertEquals(params, DiscoveryParams.fromUri(advancedUri));
  }

  @Test
  public void testFromUri_customScopes() {
    final DiscoveryParams endingSoonParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build();
    final Uri endingSoonUri = Uri.parse("https://www.kickstarter.com/discover/ending-soon");
    assertEquals(endingSoonParams, DiscoveryParams.fromUri(endingSoonUri));

    final DiscoveryParams mostFundedParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.MOST_FUNDED).build();
    final Uri mostFundedUri = Uri.parse("https://www.kickstarter.com/discover/most-funded");
    assertEquals(mostFundedParams, DiscoveryParams.fromUri(mostFundedUri));

    final DiscoveryParams newestParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.NEWEST).staffPicks(true).build();
    final Uri newestUri = Uri.parse("https://www.kickstarter.com/discover/newest");
    assertEquals(newestParams, DiscoveryParams.fromUri(newestUri));

    final DiscoveryParams popularParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build();
    final Uri popularUri = Uri.parse("https://www.kickstarter.com/discover/popular");
    assertEquals(popularParams, DiscoveryParams.fromUri(popularUri));

    final DiscoveryParams recentlyLaunchedParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.NEWEST).build();
    final Uri recentlyLaunchedUri = Uri.parse("https://www.kickstarter.com/discover/recently-launched");
    assertEquals(recentlyLaunchedParams, DiscoveryParams.fromUri(recentlyLaunchedUri));

    final DiscoveryParams recommendedParams = DiscoveryParams.builder().staffPicks(true).build();
    final Uri recommendedUri = Uri.parse("https://www.kickstarter.com/discover/recommended");
    assertEquals(recommendedParams, DiscoveryParams.fromUri(recommendedUri));

    final DiscoveryParams smallProjectsParams = DiscoveryParams.builder().pledged(0).build();
    final Uri smallProjectsUri = Uri.parse("https://www.kickstarter.com/discover/small-projects");
    assertEquals(smallProjectsParams, DiscoveryParams.fromUri(smallProjectsUri));

    final DiscoveryParams socialParams = DiscoveryParams.builder().social(0).build();
    final Uri socialUri = Uri.parse("https://www.kickstarter.com/discover/social");
    assertEquals(socialParams, DiscoveryParams.fromUri(socialUri));

    final DiscoveryParams successfulParams = DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).state(DiscoveryParams.State.SUCCESSFUL).build();
    final Uri successfulUri = Uri.parse("https://www.kickstarter.com/discover/successful");
    assertEquals(successfulParams, DiscoveryParams.fromUri(successfulUri));
  }

  @Test
  public void testFromUri_pagination() {
    final DiscoveryParams params = DiscoveryParams.builder().page(5).perPage(21).build();

    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?page=5&per_page=21");
    assertEquals(params, DiscoveryParams.fromUri(uri));
  }

  @Test
  public void testFromUri_sort() {
    final DiscoveryParams params = DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build();

    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?sort=popularity");
    assertEquals(params, DiscoveryParams.fromUri(uri));
  }

  @Test
  public void testFromUri_term() {
    final DiscoveryParams params = DiscoveryParams.builder().term("skull graphic tee").build();

    final Uri advancedUri = Uri.parse("https://www.kickstarter.com/discover/advanced?term=skull+graphic+tee");
    assertEquals(params, DiscoveryParams.fromUri(advancedUri));

    final Uri searchUri = Uri.parse("https://www.kickstarter.com/projects/search?term=skull+graphic+tee");
    assertEquals(params, DiscoveryParams.fromUri(searchUri));
  }
}
