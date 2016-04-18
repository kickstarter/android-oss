package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class BooleanUtilsTest extends TestCase {

  public void testIsTrue() {
    assertTrue(BooleanUtils.isTrue(true));
    assertFalse(BooleanUtils.isTrue(false));
    assertFalse(BooleanUtils.isTrue(null));
  }

  public void testIsFalse() throws Exception {
    assertFalse(BooleanUtils.isFalse(true));
    assertTrue(BooleanUtils.isFalse(false));
    assertTrue(BooleanUtils.isFalse(null));
  }
}
