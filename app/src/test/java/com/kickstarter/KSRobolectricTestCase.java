package com.kickstarter;

import android.content.Context;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Koala;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.MockTrackingClient;
import com.kickstarter.mock.MockCurrentConfig;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.mock.services.MockApolloClient;
import com.kickstarter.mock.services.MockWebClient;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = ShadowMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public abstract class KSRobolectricTestCase extends TestCase {
  private TestKSApplication application;
  public TestSubscriber<String> koalaTest;
  private Environment environment;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    final MockTrackingClient testTrackingClient = new MockTrackingClient(new MockCurrentUser());
    this.koalaTest = new TestSubscriber<>();
    testTrackingClient.eventNames.subscribe(this.koalaTest);
    DateTimeUtils.setCurrentMillisFixed(new DateTime().getMillis());

    this.environment = application().component().environment().toBuilder()
      .apiClient(new MockApiClient())
      .apolloClient(new MockApolloClient())
      .currentConfig(new MockCurrentConfig())
      .webClient(new MockWebClient())
      .koala(new Koala(testTrackingClient))
      .build();
  }

  protected @NonNull TestKSApplication application() {
    if (this.application != null) {
      return this.application;
    }

    this.application = (TestKSApplication) RuntimeEnvironment.application;
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
}
