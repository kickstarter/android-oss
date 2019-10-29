package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class CheckoutViewModelTest extends KSRobolectricTestCase {
  private CheckoutViewModel.ViewModel vm;
  private final TestSubscriber<String> title = new TestSubscriber<>();
  private final TestSubscriber<String> url = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new CheckoutViewModel.ViewModel(environment);
    this.vm.outputs.title().subscribe(this.title);
    this.vm.outputs.url().subscribe(this.url);
  }

  @Test
  public void test_Title_FromIntent() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.TOOLBAR_TITLE, "Test"));

    this.title.assertValue("Test");
  }

  @Test
  public void test_Url_FromIntent() {
    setUpEnvironment(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.URL, "www.test.com"));

    this.url.assertValue("www.test.com");

    this.vm.inputs.pageIntercepted("www.test2.com");
    this.url.assertValues("www.test.com", "www.test2.com");
  }
}
