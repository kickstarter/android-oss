package com.kickstarter.libs.utils;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.services.DiscoveryParams;

import junit.framework.TestCase;

import static com.kickstarter.libs.utils.DiscoveryParamsUtils.refTag;

public class DiscoveryParamsUtilsTest extends TestCase {

  public void testRefTag() {
    assertEquals(
      refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build()),
      RefTag.category()
    );

    assertEquals(
      refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build()),
      RefTag.category(DiscoveryParams.Sort.POPULAR)
    );

    assertEquals(
      refTag(DiscoveryParams.builder().location(LocationFactory.germany()).build()),
      RefTag.city()
    );

    assertEquals(
      refTag(DiscoveryParams.builder().staffPicks(true).build()),
      RefTag.recommended()
    );

    assertEquals(
      refTag(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR).build()),
      RefTag.recommended(DiscoveryParams.Sort.POPULAR)
    );

    assertEquals(
      refTag(DiscoveryParams.builder().social(1).build()),
      RefTag.social()
    );

    assertEquals(
      refTag(DiscoveryParams.builder().term("art").build()),
      RefTag.search()
    );

    assertEquals(
      refTag(DiscoveryParams.builder().build()),
      RefTag.discovery()
    );
  }
}
