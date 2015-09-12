package com.kickstarter;

import com.kickstarter.libs.KSColorUtils;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class KSColorUtilsTest extends TestCase {
  @Test
  public void testDarken() {
    assertEquals(0xFF1DB75B, KSColorUtils.darken(0xFF2BDE73, 0.2f));
  }

  @Test
  public void testDarkenWithAlpha() {
    assertEquals(0xCC1DB75B, KSColorUtils.darken(0xCC2BDE73, 0.2f));
  }

  @Test
  public void testLighten() {
    assertEquals(0xFF55E58F, KSColorUtils.lighten(0xFF2BDE73, 0.2f));
  }

  @Test
  public void testLightenWithAlpha() {
    assertEquals(0xCC55E58F, KSColorUtils.lighten(0xCC2BDE73, 0.2f));
  }
}
