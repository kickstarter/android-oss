package com.kickstarter.libs.utils;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;

import junit.framework.Assert;

import org.junit.Test;

public class PairUtilsTest extends KSRobolectricTestCase {
  @Test
  public void first() throws Exception {
    final Pair pair1 = Pair.create("3", 1);
    Assert.assertEquals("3", PairUtils.first(pair1));

    final Pair pair2 = new Pair(pair1, 3);
    Assert.assertEquals(pair1, PairUtils.first(pair2));
  }
  @Test
  public void second() throws Exception {
    final Pair pair1 = new Pair<>("3", 1);
    Assert.assertEquals(1, PairUtils.second(pair1));

    final Pair pair2 = new Pair<>(3, pair1);
    Assert.assertEquals(pair1, PairUtils.second(pair2));
  }
}
