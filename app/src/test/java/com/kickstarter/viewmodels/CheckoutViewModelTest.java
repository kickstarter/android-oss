package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Environment;

import junit.framework.Assert;

import org.junit.Test;

public final class CheckoutViewModelTest extends KSRobolectricTestCase {
  private CheckoutViewModel.ViewModel vm;

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CheckoutViewModel.ViewModel(environment);
  }

  @Test
  public void test_AndroidPayIsAvailable_WhenNotCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(false))
      .build();

    setUpEnvironment(env);

    this.vm.outputs.isAndroidPayAvailable().subscribe(Assert::assertFalse);
  }

  @Test
  public void test_AndroidPayIsAvailable_WhenCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(true))
      .build();

    setUpEnvironment(env);

    this.vm.outputs.isAndroidPayAvailable().subscribe(Assert::assertTrue);
  }
}
