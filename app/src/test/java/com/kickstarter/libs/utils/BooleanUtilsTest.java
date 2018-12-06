package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class BooleanUtilsTest extends TestCase {

  public void testIsTrue() {
    assertTrue(BooleanUtils.isTrue(true));
    assertFalse(BooleanUtils.isTrue(false));
    assertFalse(BooleanUtils.isTrue(null));
  }

  public void testIsIntTrue() {
    assertTrue(BooleanUtils.isIntTrue(1));
    assertFalse(BooleanUtils.isIntTrue(0));
    assertFalse(BooleanUtils.isIntTrue(0));
  }

  public void testIsFalse() {
    assertFalse(BooleanUtils.isFalse(true));
    assertTrue(BooleanUtils.isFalse(false));
    assertTrue(BooleanUtils.isFalse(null));
  }
}
