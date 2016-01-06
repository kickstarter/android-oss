package com.kickstarter.ui.intents;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

public final class DiscoveryIntentActionTest extends KSRobolectricTestCase {
  @Test
  public void emitsWithParamsParcelable() {
    final DiscoveryParams params = DiscoveryParams.builder().build();
    final Intent intent = new Intent().putExtra(IntentKey.DISCOVERY_PARAMS, params);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create());
    intentAction.intent(intent);

    resultTest.assertValues(params);
  }

  @Test
  public void params_emitsWithDiscoveryUri() {
    final Uri uri = Uri.parse("https://www.kickstarter.com/discover");
    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

    final TestSubscriber<DiscoveryParams> resultTest = TestSubscriber.create();
    final DiscoveryIntentAction intentAction = new DiscoveryIntentAction(resultTest::onNext, PublishSubject.create());
    intentAction.intent(intent);

    resultTest.assertValues(DiscoveryParams.builder().build());
  }
}
