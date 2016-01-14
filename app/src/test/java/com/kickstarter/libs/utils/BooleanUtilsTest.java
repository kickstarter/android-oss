package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class BooleanUtilsTest extends TestCase {

  public void testIsTrue() {
    assertTrue(BooleanUtils.isTrue(true));
    assertFalse(BooleanUtils.isTrue(false));
    assertFalse(BooleanUtils.isTrue(null));
  }
}
