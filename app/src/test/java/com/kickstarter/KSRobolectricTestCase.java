package com.kickstarter;

import android.Manifest;
import android.content.Context;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.MockTrackingClient;
import com.kickstarter.libs.TrackingClientType;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.MockExperimentsClientType;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.mock.services.MockApolloClient;
import com.kickstarter.mock.services.MockWebClient;
import com.stripe.android.Stripe;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(shadows = ShadowAndroidXMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public abstract class KSRobolectricTestCase extends TestCase {
  private TestKSApplication application;
  public TestSubscriber<String> experimentsTest;
  public TestSubscriber<String> koalaTest;
  public TestSubscriber<String> lakeTest;
  public TestSubscriber<String> segmentTest;
  private Environment environment;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    final MockCurrentConfig mockCurrentConfig = new MockCurrentConfig();

    final MockExperimentsClientType experimentsClientType = experimentsClient();
    final MockTrackingClient koalaTrackingClient = koalaTrackingClient(mockCurrentConfig, experimentsClientType);
    final MockTrackingClient lakeTrackingClient = lakeTrackingClient(mockCurrentConfig, experimentsClientType);
    final MockTrackingClient segmentTrackingClient = segmentTrackingClient(mockCurrentConfig, experimentsClientType);

    DateTimeUtils.setCurrentMillisFixed(new DateTime().getMillis());

    this.environment = application().component().environment().toBuilder()
      .apiClient(new MockApiClient())
      .apolloClient(new MockApolloClient())
      .currentConfig(mockCurrentConfig)
      .webClient(new MockWebClient())
      .stripe(new Stripe(context(), Secrets.StripePublishableKey.STAGING))
      .koala(new Koala(koalaTrackingClient))
      .lake(new Koala(lakeTrackingClient))
      .optimizely(experimentsClientType)
      .segment(new Koala(segmentTrackingClient))
      .build();
  }

  protected @NonNull TestKSApplication application() {
    if (this.application != null) {
      return this.application;
    }

    this.application = (TestKSApplication) RuntimeEnvironment.application;

    ShadowApplication app = Shadows.shadowOf(this.application);
    app.grantPermissions(Manifest.permission.INTERNET);

    return this.application;
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    DateTimeUtils.setCurrentMillisSystem();
  }

  protected @NonNull Context context() {
    return application().getApplicationContext();
  }

  protected @NonNull Environment environment() {
    return this.environment;
  }

  protected @NonNull KSString ksString() {
    return new KSString(application().getPackageName(), application().getResources());
  }

  private MockExperimentsClientType experimentsClient() {
    this.experimentsTest = new TestSubscriber<>();
    final MockExperimentsClientType experimentsClientType = new MockExperimentsClientType();
    experimentsClientType.getEventKeys().subscribe(this.experimentsTest);
    return experimentsClientType;
  }

  private MockTrackingClient koalaTrackingClient(final @NonNull MockCurrentConfig mockCurrentConfig,
    final @NonNull MockExperimentsClientType experimentsClientType) {
    final MockTrackingClient koalaTrackingClient = new MockTrackingClient(new MockCurrentUser(),
      mockCurrentConfig, TrackingClientType.Type.KOALA, experimentsClientType);
    this.koalaTest = new TestSubscriber<>();
    koalaTrackingClient.eventNames.subscribe(this.koalaTest);
    return koalaTrackingClient;
  }

  private MockTrackingClient lakeTrackingClient(final @NonNull MockCurrentConfig mockCurrentConfig,
    final @NonNull MockExperimentsClientType experimentsClientType) {
    final MockTrackingClient lakeTrackingClient = new MockTrackingClient(new MockCurrentUser(),
      mockCurrentConfig, TrackingClientType.Type.LAKE, experimentsClientType);
    this.lakeTest = new TestSubscriber<>();
    lakeTrackingClient.eventNames.subscribe(this.lakeTest);
    return lakeTrackingClient;
  }

  private MockTrackingClient segmentTrackingClient(MockCurrentConfig mockCurrentConfig, MockExperimentsClientType experimentsClientType){
      final MockTrackingClient segmentTrackingClient = new MockTrackingClient(new MockCurrentUser(),
              mockCurrentConfig, TrackingClientType.Type.SEGMENT, experimentsClientType);
      this.segmentTest = new TestSubscriber<>();
    segmentTrackingClient.eventNames.subscribe(this.segmentTest);
      return segmentTrackingClient;
  }
}
