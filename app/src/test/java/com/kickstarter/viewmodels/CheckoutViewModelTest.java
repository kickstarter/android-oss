package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.AndroidPayCapability;
import com.kickstarter.libs.Environment;

import junit.framework.Assert;

import org.junit.Test;

public final class CheckoutViewModelTest extends KSRobolectricTestCase {

  @Test
  public void test_AndroidPayIsAvailable_WhenNotCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(false))
      .build();

    final CheckoutViewModel vm = new CheckoutViewModel(env);

    vm.outputs.isAndroidPayAvailable().subscribe(Assert::assertFalse);
  }

  @Test
  public void test_AndroidPayIsAvailable_WhenCapable() {
    final Environment env = environment()
      .toBuilder()
      .androidPayCapability(new AndroidPayCapability(true))
      .build();

    final CheckoutViewModel vm = new CheckoutViewModel(env);

    vm.outputs.isAndroidPayAvailable().subscribe(Assert::assertTrue);
  }
}
