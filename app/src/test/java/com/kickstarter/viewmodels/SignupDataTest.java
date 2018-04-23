package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

public class SignupDataTest extends KSRobolectricTestCase {
  @Test
  public void testSignupData_isValid() {
    assertTrue((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("", "b@kickstarter.com", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter", "danisawesome", true)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter.com", "dan", true)).isValid());
    assertTrue((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter.com", "danisawesome", false)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("", "b@kickstarter.com", "danisawesome", false)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter", "danisawesome", false)).isValid());
    assertFalse((new SignupViewModel.ViewModel.SignupData("brando", "b@kickstarter.com", "dan", false)).isValid());
  }
}
