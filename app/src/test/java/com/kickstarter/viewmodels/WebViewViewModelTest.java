package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.mock.factories.PushNotificationEnvelopeFactory;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public final class WebViewViewModelTest extends KSRobolectricTestCase {
  private WebViewViewModel.ViewModel vm;
  private final TestSubscriber<String> toolbarTitle = new TestSubscriber<>();
  private final TestSubscriber<String> url = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new WebViewViewModel.ViewModel(environment);
    this.vm.outputs.toolbarTitle().subscribe(this.toolbarTitle);
    this.vm.outputs.url().subscribe(this.url);
  }

  @Test
  public void testToolbarTitle() {
    final String toolbarTitle = "some body once told me";
    setUpEnvironment(environment());

    this.vm.intent(new Intent().putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle));
    this.toolbarTitle.assertValues(toolbarTitle);
  }

  @Test
  public void testUrl() {
    final String url = "d.rip";
    setUpEnvironment(environment());

    this.vm.intent(new Intent().putExtra(IntentKey.URL, url));
    this.url.assertValues(url);
  }
}
