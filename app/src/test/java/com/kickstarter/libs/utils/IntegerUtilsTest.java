package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class IntegerUtilsTest extends TestCase {

  public void testIsNonZero() {
    assertTrue(IntegerUtils.isNonZero(1));
    assertTrue(IntegerUtils.isNonZero(-1));
    assertFalse(IntegerUtils.isNonZero(0));
    assertFalse(IntegerUtils.isNonZero(null));
  }

  public void testIsZero() {
    assertFalse(IntegerUtils.isZero(1));
    assertFalse(IntegerUtils.isZero(-1));
    assertTrue(IntegerUtils.isZero(0));
    assertFalse(IntegerUtils.isZero(null));
  }

  public void testSafeUnboxWithDefaultValueZero() {
    assertEquals(5 , IntegerUtils.safeUnboxWithDefaultValueZero(5));
    assertEquals(0, IntegerUtils.safeUnboxWithDefaultValueZero(null));
  }
}