package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.ui.intentmappers.IntentMapper;

import org.junit.Test;

public class IntentMapperTest extends KSRobolectricTestCase {

  @Test
  public void testIntentMapper_EmitsFromAppBanner() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final Uri appBannerUri = Uri.parse("https://www.kickstarter.com/?app_banner=1");
    final Intent appBannerIntent = new Intent(Intent.ACTION_VIEW, appBannerUri);

    assertFalse(IntentMapper.intentFromAppBanner(intent));
    assertTrue(IntentMapper.intentFromAppBanner(appBannerIntent));
  }
}
