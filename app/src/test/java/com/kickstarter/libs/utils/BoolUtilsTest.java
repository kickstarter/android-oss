package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class BoolUtilsTest extends TestCase {

  public void testIsTrue() {
    assertTrue(BoolUtils.isTrue(true));
    assertFalse(BoolUtils.isTrue(false));
    assertFalse(BoolUtils.isTrue(null));
  }
}