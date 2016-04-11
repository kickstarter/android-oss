package com.kickstarter.libs.utils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {

  public void testIsEmail() {
    assertTrue(StringUtils.isEmail("hello@kickstarter.com"));
  }

  public void testIsEmpty() {
    assertTrue(StringUtils.isEmpty(""));
    assertTrue(StringUtils.isEmpty(" "));
    assertTrue(StringUtils.isEmpty("     "));
    assertTrue(StringUtils.isEmpty(null));
    assertFalse(StringUtils.isEmpty("a"));
    assertFalse(StringUtils.isEmpty(" a "));
  }

  public void testIsPresent() {
    assertFalse(StringUtils.isPresent(""));
    assertFalse(StringUtils.isPresent(" "));
    assertFalse(StringUtils.isPresent("     "));
    assertFalse(StringUtils.isPresent(null));
    assertTrue(StringUtils.isPresent("a"));
    assertTrue(StringUtils.isPresent(" a "));
  }
}
