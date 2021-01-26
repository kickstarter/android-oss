package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;

import com.kickstarter.KSRobolectricTestCase;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class DeepLinkViewModelTest extends KSRobolectricTestCase {
  private DeepLinkViewModel.ViewModel vm;
  private final TestSubscriber<String> startBrowser = new TestSubscriber<>();
  private final TestSubscriber<Void> startDiscoveryActivity = new TestSubscriber<>();
  private final TestSubscriber<Uri> startProjectActivity = new TestSubscriber<>();
  private final TestSubscriber<Uri> startProjectActivityForCheckout = new TestSubscriber<>();

  protected void setUpEnvironment() {
    this.vm = new DeepLinkViewModel.ViewModel(environment());
    this.vm.outputs.startBrowser().subscribe(this.startBrowser);
    this.vm.outputs.startDiscoveryActivity().subscribe(this.startDiscoveryActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.startProjectActivityForCheckout().subscribe(this.startProjectActivityForCheckout);
  }

  @Test
  public void testNonDeepLink_startsBrowser() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/comments";
    this.vm.intent(intentWithData(url));

    this.startBrowser.assertValue(url);
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertNoValues();
  }

  @Test
  public void testProjectPreviewLink_startsBrowser() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?token=beepboop";
    this.vm.intent(intentWithData(url));

    this.startBrowser.assertValue(url);
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertNoValues();
  }

  @Test
  public void testCheckoutDeepLinkWithRefTag_startsProjectActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=discovery";
    this.vm.intent(intentWithData(url));

    this.startProjectActivity.assertNoValues();
    this.startBrowser.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertValue(Uri.parse(url));
  }

  @Test
  public void testCheckoutDeepLinkWithoutRefTag_startsProjectActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge";
    this.vm.intent(intentWithData(url));

    final String expectedUrl = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap/pledge?ref=android_deep_link";
    this.startProjectActivity.assertNoValues();
    this.startBrowser.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertValue(Uri.parse(expectedUrl));
  }

  @Test
  public void testProjectDeepLinkWithRefTag_startsProjectActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=discovery";
    this.vm.intent(intentWithData(url));

    this.startProjectActivity.assertValue(Uri.parse(url));
    this.startBrowser.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertNoValues();
  }

  @Test
  public void testProjectDeepLinkWithoutRefTag_startsProjectActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap";
    this.vm.intent(intentWithData(url));

    final String expectedUrl = "https://www.kickstarter.com/projects/smithsonian/smithsonian-anthology-of-hip-hop-and-rap?ref=android_deep_link";
    this.startProjectActivity.assertValue(Uri.parse(expectedUrl));
    this.startBrowser.assertNoValues();
    this.startDiscoveryActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertNoValues();
  }

  @Test
  public void testDiscoveryDeepLink_startsDiscoveryActivity() {
    setUpEnvironment();

    final String url = "https://www.kickstarter.com/projects";
    this.vm.intent(intentWithData(url));

    this.startDiscoveryActivity.assertValueCount(1);
    this.startBrowser.assertNoValues();
    this.startProjectActivity.assertNoValues();
    this.startProjectActivityForCheckout.assertNoValues();
  }

  private Intent intentWithData(final String url) {
    return new Intent()
      .setData(Uri.parse(url));
  }
}
