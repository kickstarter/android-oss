package com.kickstarter.libs.utils;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.services.DiscoveryParams;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DiscoveryParamsUtilsTest {

  @Test
  public void testRefTag() {
    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build()),
      RefTag.category()
    );

    assertEquals(
      RefTag.category(DiscoveryParams.Sort.POPULAR),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build())
    );

    assertEquals(
      RefTag.city(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().location(LocationFactory.germany()).build())
    );

    assertEquals(
      RefTag.recommended(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).build())
    );

    assertEquals(
      RefTag.recommended(DiscoveryParams.Sort.POPULAR),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR).build())
    );

    assertEquals(
      RefTag.social(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().social(1).build())
    );

    assertEquals(
      RefTag.search(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().term("art").build())
    );

    assertEquals(
      RefTag.discovery(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().build())
    );
  }

  @Test
  public void testShouldIncludePotd() {
    assertFalse(DiscoveryParams.builder().term("cat").build().shouldIncludePotd());
    assertTrue(DiscoveryParams.builder().build().shouldIncludePotd());
    assertFalse(DiscoveryParams.builder().page(2).build().shouldIncludePotd());
    assertFalse(DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build().shouldIncludePotd());
  }
}
