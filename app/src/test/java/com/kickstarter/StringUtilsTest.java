package com.kickstarter;

import com.kickstarter.libs.utils.StringUtils;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase {
  public void testFlooredPercentageStringForFormat() {
    assertEquals("5%", StringUtils.displayFlooredPercentage(5.0f));
    assertEquals("5%", StringUtils.displayFlooredPercentage(5.1234f));
    assertEquals("5%", StringUtils.displayFlooredPercentage(5.9876f));
  }
}
