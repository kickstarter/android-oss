package com.kickstarter;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;

public class KSRobolectricGradleTestRunner extends RobolectricTestRunner {
  public static final int DEFAULT_SDK = 21;

  public KSRobolectricGradleTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }
}
