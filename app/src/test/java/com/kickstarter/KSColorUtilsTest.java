package com.kickstarter;

import com.kickstarter.libs.KSColorUtils;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
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

  @Test
  public void testArtIsLight() {
    assertEquals(true, KSColorUtils.isLight(0xFFFFBDAB));
  }

  @Test
  public void testComicsIsLight() {
    assertEquals(true, KSColorUtils.isLight(0xFFFFFB78));
  }

  @Test
  public void testCraftsIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFFF81AC));
  }

  @Test
  public void testDanceIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFA695F9));
  }

  @Test
  public void testDesignIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFF2752FF));
  }

  @Test
  public void testFashionIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFFF9FD6));
  }

  @Test
  public void testFilmAndVideoIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFFF596E));
  }

  @Test
  public void testFoodIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFFF3642));
  }

  @Test
  public void testGamesIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFF00C9AB));
  }

  @Test
  public void testJournalismIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFF12BCEA));
  }

  @Test
  public void testMusicIsLight() {
    assertEquals(true, KSColorUtils.isLight(0xFFA5FFD3));
  }

  @Test
  public void testPhotographyIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFF00E3E5));
  }

  @Test
  public void testPublishingIsLight() {
    assertEquals(true, KSColorUtils.isLight(0xFFE2DCD0));
  }

  @Test
  public void testTechnologyIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFF6396FC));
  }

  @Test
  public void testTheaterIsNotLight() {
    assertEquals(false, KSColorUtils.isLight(0xFFFF7D5F));
  }

  @Test
  public void testBlackIsDark() {
    assertEquals(true, KSColorUtils.isDark(0xFF000000));
  }

  @Test
  public void testWhiteIsNotDark() {
    assertEquals(false, KSColorUtils.isDark(0xFFFFFFFF));
  }
}
