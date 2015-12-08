package com.kickstarter;

import android.support.annotation.NonNull;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = ShadowMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class KSRobolectricTestCase extends TestCase {
  private TestKSApplication application;

  protected @NonNull TestKSApplication application() {
    if (application != null) {
      return application;
    }

    application = (TestKSApplication) RuntimeEnvironment.application;
    return application;
  }
}
