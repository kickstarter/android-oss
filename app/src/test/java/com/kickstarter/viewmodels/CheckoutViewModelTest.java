package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ActivityResultFactory;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Environment;

import org.junit.Test;

import rx.observers.TestSubscriber;

public final class CheckoutViewModelTest extends KSRobolectricTestCase {
  private CheckoutViewModel.ViewModel vm;
  private final TestSubscriber<Integer> androidPayError = new TestSubscriber<>();
  private final TestSubscriber<Boolean> isAndroidPayAvailable = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CheckoutViewModel.ViewModel(environment);
    this.vm.outputs.androidPayError().subscribe(this.androidPayError);
    this.vm.outputs.isAndroidPayAvailable().subscribe(this.isAndroidPayAvailable);
  }

  @Test
  public void test_AndroidPayError() {
    setUpEnvironment(environment());

    this.vm.activityResult(ActivityResultFactory.androidPayErrorResult());
    this.androidPayError.assertValueCount(1);
  }

  @Test
  public void test_AndroidPayIsAvailable_WhenNotCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(false))
      .build();

    setUpEnvironment(env);
    this.isAndroidPayAvailable.assertValues(false);
  }

  @Test
  public void test_AndroidPayIsAvailable_WhenCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(true))
      .build();

    setUpEnvironment(env);
    this.isAndroidPayAvailable.assertValues(true);
  }
}
