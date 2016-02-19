package com.kickstarter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSString;

import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows = ShadowMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public abstract class KSRobolectricTestCase extends TestCase {
  private TestKSApplication application;

  protected @NonNull TestKSApplication application() {
    if (application != null) {
      return application;
    }

    application = (TestKSApplication) RuntimeEnvironment.application;
    return application;
  }

  protected @NonNull Context context() {
    return application().getApplicationContext();
  }

  protected @NonNull Environment environment() {
    return application().component().environment();
  }

  protected @NonNull KSString ksString() {
    return new KSString(application().getPackageName(), application().getResources());
  }
}
