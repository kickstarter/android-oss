package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.intentmappers.DiscoveryIntentMapper;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class DiscoveryIntentMapperTest extends KSRobolectricTestCase {
  @Test
  public void emitsFromParamsExtra() {
    final DiscoveryParams params = DiscoveryParams.builder().build();
    final Intent intent = new Intent().putExtra(IntentKey.DISCOVERY_PARAMS, params);
    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();

    DiscoveryIntentMapper.params(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValues(params);
  }

  @Test
  public void emitsFromDiscoveryUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    DiscoveryIntentMapper.params(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValues(DiscoveryParams.builder().build());
  }

  @Test
  public void emitsFromDiscoveryCategoryUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/categories/music");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    DiscoveryIntentMapper.params(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(1);
  }

  @Test
  public void emitsFromDiscoveryLocationUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    DiscoveryIntentMapper.params(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(1);
  }

  @Test
  public void emitsFromAdvancedCategoryIdAndLocationIdUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?category_id=1&location_id=1");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    DiscoveryIntentMapper.params(intent, new MockApiClient())
      .subscribe(resultTest);

    resultTest.assertValueCount(1);
  }
}
