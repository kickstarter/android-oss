package com.kickstarter.viewmodels;

import com.kickstarter.BuildConfig;
import com.kickstarter.KSRobolectricGradleTestRunner;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.multidex.ShadowMultiDex;

import rx.observers.TestSubscriber;

@RunWith(KSRobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows=ShadowMultiDex.class, sdk = KSRobolectricGradleTestRunner.DEFAULT_SDK)
public class SignupViewModelTest extends TestCase {

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
