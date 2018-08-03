package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.KoalaEvent;

import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

public class DeepLinkViewModelTest extends KSRobolectricTestCase {
  private DeepLinkViewModel.ViewModel vm;
  private final TestSubscriber<Void> requestPackageManager = new TestSubscriber<>();
  private final TestSubscriber<List<Intent>> startBrowser = new TestSubscriber<>();
  private final TestSubscriber<Void> startDiscoveryActivity = new TestSubscriber<>();
  private final TestSubscriber<Uri> startProjectActivity = new TestSubscriber<>();

  protected void setUpEnvironment() {
    this.vm = new DeepLinkViewModel.ViewModel(environment());
    this.vm.outputs.requestPackageManager().subscribe(this.requestPackageManager);
    this.vm.outputs.startBrowser().subscribe(this.startBrowser);
    this.vm.outputs.startDiscoveryActivity().subscribe(this.startDiscoveryActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
  }

  @Test
  public void testNonDeepLink_startsBrowser() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/comments";
    this.vm.intent(intentWithData(url));
    this.vm.packageManager(application().getPackageManager());

    this.requestPackageManager.assertValueCount(1);
    this.startBrowser.assertValueCount(1);
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.koalaTest.assertNoValues();
  }

  @Test
  public void testProjectPreviewLink_startsBrowser() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?token=beepboop";
    this.vm.intent(intentWithData(url));
    this.vm.packageManager(application().getPackageManager());

    this.requestPackageManager.assertValueCount(1);
    this.startBrowser.assertValueCount(1);
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.koalaTest.assertNoValues();
  }

  @Test
  public void testProjectDeepLink_startsProjectActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap";
    this.vm.intent(intentWithData(url));

    this.startProjectActivity.assertValue(Uri.parse(url));
    this.startBrowser.assertNoValues();
    this.requestPackageManager.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.CONTINUE_USER_ACTIVITY, KoalaEvent.OPENED_DEEP_LINK);
  }

  @Test
  public void testDiscoveryDeepLink_startsDiscoveryActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects";
    this.vm.intent(intentWithData(url));

    this.startDiscoveryActivity.assertValueCount(1);
    this.startBrowser.assertNoValues();
    this.requestPackageManager.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.CONTINUE_USER_ACTIVITY, KoalaEvent.OPENED_DEEP_LINK);
  }

  private Intent intentWithData(final String url) {
    return new Intent()
      .setData(Uri.parse(url));
  }
}
