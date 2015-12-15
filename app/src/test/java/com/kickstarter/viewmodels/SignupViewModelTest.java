package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class SignupViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testSignupViewModel_formValidation() {

    final SignupViewModel p = new SignupViewModel();
    final TestSubscriber<Boolean> test = new TestSubscriber<>();
    p.outputs.formIsValid().subscribe(test);
    
    p.inputs.fullName("brandon");
    test.assertNoValues();

    p.inputs.email("hello@kickstarter.com");
    test.assertNoValues();

    p.inputs.password("danisawesome");
    test.assertNoValues();

    p.inputs.sendNewsletters(true);
    test.assertValues(true);

    p.inputs.email("incorrect@kickstarter");
    test.assertValues(true, false);
  }
}
