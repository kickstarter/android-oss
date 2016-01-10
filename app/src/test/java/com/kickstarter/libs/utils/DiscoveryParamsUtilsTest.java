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
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder()
        .category(CategoryFactory.artCategory())
        .sort(DiscoveryParams.Sort.POPULAR)
        .build()),
      RefTag.category(DiscoveryParams.Sort.POPULAR)
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().location(LocationFactory.germany()).build()),
      RefTag.city()
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).build()),
      RefTag.recommended()
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR).build()),
      RefTag.recommended(DiscoveryParams.Sort.POPULAR)
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().social(1).build()),
      RefTag.social()
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().term("art").build()),
      RefTag.search()
    );

    assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().build()),
      RefTag.discovery()
    );
  }
}
