package com.kickstarter.presenters;

import com.kickstarter.BuildConfig;
import com.kickstarter.KSRobolectricGradleTestRunner;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class SignupDataTest extends TestCase {
  @Test
  public void testSignupData_isValid() {
    assertTrue((new SignupPresenter.SignupData("brando", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupPresenter.SignupData("", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupPresenter.SignupData("brando", "b@kickstarter", "danisawesome", true)).isValid());
    assertFalse((new SignupPresenter.SignupData("brando", "b@kickstarter.com", "dan", true)).isValid());
  }
}
