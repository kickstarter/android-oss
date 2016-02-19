package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class SignupViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testSignupViewModel_formValidation() {
    final SignupViewModel p = new SignupViewModel(environment());
    final TestSubscriber<Boolean> test = new TestSubscriber<>();
    p.outputs.formIsValid().subscribe(test);
    
    p.inputs.fullName("brandon");
    test.assertNoValues();

    p.inputs.email("hello@kickstarter.com");
    test.assertNoValues();

    p.inputs.password("danisawesome");
    test.assertNoValues();

    p.inputs.sendNewslettersClick(true);
    test.assertValues(true);

    p.inputs.email("incorrect@kickstarter");
    test.assertValues(true, false);
  }
}
