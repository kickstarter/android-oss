package com.kickstarter.viewmodels;

import com.kickstarter.BuildConfig;
import com.kickstarter.KSRobolectricGradleTestRunner;
import com.kickstarter.KSRobolectricTestCase;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

public class SignupDataTest extends KSRobolectricTestCase {
  @Test
  public void testSignupData_isValid() {
    assertTrue((new SignupViewModel.SignupData("brando", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.SignupData("", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.SignupData("brando", "b@kickstarter", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.SignupData("brando", "b@kickstarter.com", "dan", true)).isValid());
  }
}
