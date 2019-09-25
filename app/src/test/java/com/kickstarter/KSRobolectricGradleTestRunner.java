package com.kickstarter;

import android.os.Build;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

public class KSRobolectricGradleTestRunner extends RobolectricTestRunner {
  static final int DEFAULT_SDK = Build.VERSION_CODES.M;

  public KSRobolectricGradleTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }
}
