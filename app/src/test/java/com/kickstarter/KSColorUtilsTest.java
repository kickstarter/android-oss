package com.kickstarter;

import com.kickstarter.libs.utils.KSColorUtils;

import org.junit.Test;

public class KSColorUtilsTest extends KSRobolectricTestCase {
  @Test
  public void testSetAlpha() {
    assertEquals(0x00FFFFFF, KSColorUtils.setAlpha(0xFFFFFFFF, 0));
    assertEquals(0xFFFFFFFF, KSColorUtils.setAlpha(0x00FFFFFF, 255));
    assertEquals(0x0ACCCCCC, KSColorUtils.setAlpha(0xCCCCCC, 10));
    assertEquals(0x0ACCCCCC, KSColorUtils.setAlpha(0xFFCCCCCC, 10));
  }

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
    assertTrue(KSColorUtils.isLight(0xFFFFBDAB));
  }

  @Test
  public void testComicsIsLight() {
    assertTrue(KSColorUtils.isLight(0xFFFFFB78));
  }

  @Test
  public void testCraftsIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFFF81AC));
  }

  @Test
  public void testDanceIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFA695F9));
  }

  @Test
  public void testDesignIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFF2752FF));
  }

  @Test
  public void testFashionIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFFF9FD6));
  }

  @Test
  public void testFilmAndVideoIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFFF596E));
  }

  @Test
  public void testFoodIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFFF3642));
  }

  @Test
  public void testGamesIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFF00C9AB));
  }

  @Test
  public void testJournalismIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFF12BCEA));
  }

  @Test
  public void testMusicIsLight() {
    assertTrue(KSColorUtils.isLight(0xFFA5FFD3));
  }

  @Test
  public void testPhotographyIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFF00E3E5));
  }

  @Test
  public void testPublishingIsLight() {
    assertTrue(KSColorUtils.isLight(0xFFE2DCD0));
  }

  @Test
  public void testTechnologyIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFF6396FC));
  }

  @Test
  public void testTheaterIsNotLight() {
    assertFalse(KSColorUtils.isLight(0xFFFF7D5F));
  }

  @Test
  public void testBlackIsDark() {
    assertTrue(KSColorUtils.isDark(0xFF000000));
  }

  @Test
  public void testWhiteIsNotDark() {
    assertFalse(KSColorUtils.isDark(0xFFFFFFFF));
  }
}
