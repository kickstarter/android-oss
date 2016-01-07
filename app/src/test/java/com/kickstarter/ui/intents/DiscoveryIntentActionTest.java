package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class DiscoveryIntentActionTest extends KSRobolectricTestCase {
  @Test
  public void emitsStaffPicksFromEmptyIntent() {
    final DiscoveryParams params = DiscoveryParams.builder().staffPicks(true).build();

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(new Intent());

    resultTest.assertValues(params);
  }

  @Test
  public void emitsFromParamsExtra() {
    final DiscoveryParams params = DiscoveryParams.builder().build();
    final Intent intent = new Intent().putExtra(IntentKey.DISCOVERY_PARAMS, params);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValues(params);
  }

  @Test
  public void emitsFromDiscoveryUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValues(DiscoveryParams.builder().build());
  }

  @Test
  public void emitsFromDiscoveryCategoryUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/categories/music");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(1);
  }

  @Test
  public void emitsFromDiscoveryLocationUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/places/sydney-au");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(1);
  }

  @Test
  public void emitsFromAdvancedCategoryIdAndLocationIdUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover/advanced?category_id=1&location_id=1");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create(), new MockApiClient());
    intentAction.intent(intent);

    resultTest.assertValueCount(1);
  }
}
