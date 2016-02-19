package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class SignupViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testSignupViewModel_formValidation() {
    final SignupViewModel vm = new SignupViewModel(environment());
    final TestSubscriber<Boolean> test = new TestSubscriber<>();
    vm.outputs.formIsValid().subscribe(test);
    
    vm.inputs.fullName("brandon");
    test.assertNoValues();

    vm.inputs.email("hello@kickstarter.com");
    test.assertNoValues();

    vm.inputs.password("danisawesome");
    test.assertNoValues();

    vm.inputs.sendNewslettersClick(true);
    test.assertValues(true);

    vm.inputs.email("incorrect@kickstarter");
    test.assertValues(true, false);
  }
}
