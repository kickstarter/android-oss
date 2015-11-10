package com.kickstarter.presenters;

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
public class SignupPresenterTest extends TestCase {

  @Test
  public void testSignupPresenter_formValidation() {

    final SignupPresenter p = new SignupPresenter();
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
