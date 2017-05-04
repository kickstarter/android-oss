package com.kickstarter.libs.utils;

import com.kickstarter.factories.CategoryFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.libs.RefTag;
import com.kickstarter.services.DiscoveryParams;

import org.junit.Assert;
import org.junit.Test;

public class DiscoveryParamsUtilsTest {

  @Test
  public void testRefTag() {
    Assert.assertEquals(
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build()),
      RefTag.category()
    );

    Assert.assertEquals(
      RefTag.category(DiscoveryParams.Sort.POPULAR),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build())
    );

    Assert.assertEquals(
      RefTag.city(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().location(LocationFactory.germany()).build())
    );

    Assert.assertEquals(
      RefTag.recommended(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).build())
    );

    Assert.assertEquals(
      RefTag.recommended(DiscoveryParams.Sort.POPULAR),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().staffPicks(true).sort(DiscoveryParams.Sort.POPULAR).build())
    );

    Assert.assertEquals(
      RefTag.social(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().social(1).build())
    );

    Assert.assertEquals(
      RefTag.search(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().term("art").build())
    );

    Assert.assertEquals(
      RefTag.discovery(),
      DiscoveryParamsUtils.refTag(DiscoveryParams.builder().build())
    );
  }

  @Test
  public void testShouldIncludePotd() {
    Assert.assertFalse(DiscoveryParams.builder().term("cat").build().shouldIncludePotd());
    Assert.assertTrue(DiscoveryParams.builder().build().shouldIncludePotd());
    Assert.assertFalse(DiscoveryParams.builder().page(2).build().shouldIncludePotd());
    Assert.assertFalse(DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build().shouldIncludePotd());
  }
}
