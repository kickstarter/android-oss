package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.viewmodels.PlaygroundViewModel;

import org.junit.Test;

public final class PlaygroundViewModelTest extends KSRobolectricTestCase {
  private PlaygroundViewModel.ViewModel vm;

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new PlaygroundViewModel.ViewModel(environment);
  }

  @Test
  public void testSomething() {
  }
}
