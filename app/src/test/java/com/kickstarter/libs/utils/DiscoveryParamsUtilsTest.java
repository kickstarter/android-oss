package com.kickstarter.libs.utils;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.services.DiscoveryParams;

import junit.framework.TestCase;

public class DiscoveryParamsUtilsTest extends TestCase {

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
}
