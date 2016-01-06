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
    final DiscoveryParams params = DiscoveryParams.builder().categorySlug("music").build();

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
    final DiscoveryParams params = DiscoveryParams.builder().locationSlug("sydney-au").build();

    final Uri placesUri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au");
    assertEquals(params, DiscoveryParams.fromUri(placesUri));

    final Uri advancedUri = Uri.parse("https://www.kickstarter.com/discover/advanced?location_id=sydney-au");
    assertEquals(params, DiscoveryParams.fromUri(advancedUri));
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
