package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.KoalaEvent;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;

public class DeepLinkViewModelTest extends KSRobolectricTestCase {
  private DeepLinkViewModel.ViewModel vm;
  private final TestSubscriber<String> requestPackageManager = new TestSubscriber<>();
  private final TestSubscriber<List<Intent>> startBrowser = new TestSubscriber<>();
  private final TestSubscriber<Void> startDiscoveryActivity = new TestSubscriber<>();
  private final TestSubscriber<String> startProjectActivity = new TestSubscriber<>();

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

    String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/comments";
    this.vm.intent(uriIntent(url));
    this.vm.packageManager(application().getPackageManager());

    // Back button is gone if navigating from non-backer modal view.
    this.requestPackageManager.assertValue(url);
    this.startBrowser.assertValue(new ArrayList<>());
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.koalaTest.assertNoValues();
  }

  @Test
  public void testProjectDeepLink_startsProjectActivity() {
    setUpEnvironment();

    String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap";
    this.vm.intent(uriIntent(url));

    // Back button is gone if navigating from non-backer modal view.
    this.startProjectActivity.assertValue(url);
    this.startBrowser.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.CONTINUE_USER_ACTIVITY, KoalaEvent.OPENED_DEEP_LINK);
  }

  @Test
  public void testDiscoveryDeepLink_startsDiscoveryActivity() {
    setUpEnvironment();

    String url = "https://www.kickstarter.com/projects";
    this.vm.intent(uriIntent(url));

    // Back button is gone if navigating from non-backer modal view.
    this.startDiscoveryActivity.assertValueCount(1);
    this.startBrowser.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.koalaTest.assertValues(KoalaEvent.CONTINUE_USER_ACTIVITY, KoalaEvent.OPENED_DEEP_LINK);
  }

  private Intent uriIntent(String url) {
    return new Intent()
      .setData(Uri.parse(url));
  }
}
