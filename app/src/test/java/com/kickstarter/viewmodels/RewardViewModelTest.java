package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ConfigFactory;
import com.kickstarter.factories.ProjectFactory;
import com.kickstarter.factories.RewardFactory;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;

import org.joda.time.DateTime;
import org.junit.Test;

import rx.observers.TestSubscriber;

public final class RewardViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testBackers() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward rewardWithBackers = RewardFactory.reward().toBuilder()
      .backersCount(10)
      .build();

    final TestSubscriber<Integer> backersTest = TestSubscriber.create();
    vm.outputs.backers().subscribe(backersTest);

    final TestSubscriber<Boolean> backersIsHiddenTest = TestSubscriber.create();
    vm.outputs.backersIsHidden().subscribe(backersIsHiddenTest);

    // Show reward backer count.
    vm.inputs.projectAndReward(project, rewardWithBackers);

    backersTest.assertValue(10);
    backersIsHiddenTest.assertValue(false);

    // If reward is 'no reward', backers should be hidden.
    vm.inputs.projectAndReward(project, RewardFactory.noReward());
    backersTest.assertValues(10);
    backersIsHiddenTest.assertValues(false, true);
  }

  @Test
  public void testClickable() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> clickableTest = TestSubscriber.create();
    vm.outputs.clickable().subscribe(clickableTest);

    // Live project with no reward limit is clickable.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    clickableTest.assertValue(true);

    // Rewards from projects that aren't live is not clickable.
    vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward());
    clickableTest.assertValues(true, false);

    // Live project with limit reached and is not clickable if the user hasn't backed it.
    vm.inputs.projectAndReward(
      ProjectFactory.project(),
      RewardFactory.rewardWithLimitReached()
    );
    clickableTest.assertValues(true, false, false);

    // Live project with limit reached is clickable if the user has backed it.
    vm.inputs.projectAndReward(
      ProjectFactory.project().toBuilder().isBacking(true).build(),
      RewardFactory.rewardWithLimitReached()
    );
    clickableTest.assertValues(true, false, false, true);
  }

  @Test
  public void testDescription() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<String> descriptionTest = TestSubscriber.create();
    vm.outputs.description().subscribe(descriptionTest);

    vm.inputs.projectAndReward(project, reward);

    descriptionTest.assertValue(reward.description());
  }

  @Test
  public void testEstimatedDelivery() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();

    final TestSubscriber<DateTime> estimatedDeliveryTest = TestSubscriber.create();
    vm.outputs.estimatedDelivery().subscribe(estimatedDeliveryTest);

    vm.inputs.projectAndReward(project, reward);

    // If reward has no estimated delivery, no value should be emitted.
    estimatedDeliveryTest.assertNoValues();

    // Reward with estimated delivery should emit.
    final DateTime estimatedDelivery = DateTime.now();
    vm.inputs.projectAndReward(project, reward.toBuilder().estimatedDeliveryOn(estimatedDelivery).build());

    estimatedDeliveryTest.assertValue(estimatedDelivery);
  }

  @Test
  public void testEstimatedDeliveryVisible() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> estimatedDeliveryIsHiddenTest = TestSubscriber.create();
    vm.outputs.estimatedDeliveryIsHidden().subscribe(estimatedDeliveryIsHiddenTest);

    // Reward with no estimated delivery should not show estimated delivery label.
    vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(null).build());
    estimatedDeliveryIsHiddenTest.assertValue(true);

    // Reward with estimated delivery should show estimated delivery label.
    vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(DateTime.now()).build());
    estimatedDeliveryIsHiddenTest.assertValues(true, false);
  }

  @Test
  public void testLimit() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Pair<String, String>> limitAndRemainingTest = TestSubscriber.create();
    vm.outputs.limitAndRemaining().subscribe(limitAndRemainingTest);

    final TestSubscriber<Boolean> limitIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitIsHidden().subscribe(limitIsHiddenTest);

    final TestSubscriber<Boolean> allGoneIsHiddenTest = TestSubscriber.create();
    vm.outputs.allGoneIsHidden().subscribe(allGoneIsHiddenTest);

    // When reward is limited, limit/remaining should emit and limit should not be hidden.
    final Reward limitedReward = RewardFactory.reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
    vm.inputs.projectAndReward(project, limitedReward);
    limitAndRemainingTest.assertValue(Pair.create("10", "5"));
    limitIsHiddenTest.assertValue(false);
    allGoneIsHiddenTest.assertValue(true);

    // When reward's limit has been reached, show that all the items are gone.
    vm.inputs.projectAndReward(project, RewardFactory.rewardWithLimitReached());
    limitIsHiddenTest.assertValues(false, true);
    allGoneIsHiddenTest.assertValues(true, false);

    // When reward has no limit, hide the limit and all gone views.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    limitIsHiddenTest.assertValues(false, true, true);
    allGoneIsHiddenTest.assertValues(true, false, true);
  }

  @Test
  public void testMinimum() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(10)
      .build();

    final TestSubscriber<String> minimumTest = TestSubscriber.create();
    vm.outputs.minimum().subscribe(minimumTest);

    vm.inputs.projectAndReward(project, reward);

    minimumTest.assertValue("$10");
  }

  @Test
  public void testRewardClickedStartsCheckout() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<Pair<Project, Reward>> startCheckoutTest = TestSubscriber.create();
    vm.outputs.goToCheckout().subscribe(startCheckoutTest);

    vm.projectAndReward(project, reward);

    startCheckoutTest.assertNoValues();

    // When a reward is clicked, start checkout.
    vm.inputs.rewardClicked();
    startCheckoutTest.assertValue(Pair.create(project, reward));
  }

  @Test
  public void testRewardClickedForCompletedProjectDoesNotStartCheckout() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.successfulProject();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<Pair<Project, Reward>> startCheckoutTest = TestSubscriber.create();
    vm.outputs.goToCheckout().subscribe(startCheckoutTest);

    vm.projectAndReward(project, reward);

    startCheckoutTest.assertNoValues();

    // Technically the user should not be able to click rewards on successful projects, but just in case, ensure
    // the clip is a no-op.
    vm.inputs.rewardClicked();
    startCheckoutTest.assertNoValues();
  }

  @Test
  public void testSelectedRewardIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<Boolean> selectedRewardIsHidden = TestSubscriber.create();
    vm.outputs.selectedRewardIsHidden().subscribe(selectedRewardIsHidden);

    vm.inputs.projectAndReward(ProjectFactory.backedProject(), reward);
    selectedRewardIsHidden.assertValue(false);

    vm.inputs.projectAndReward(ProjectFactory.project(), reward);
    selectedRewardIsHidden.assertValues(false, true);
  }

  @Test
  public void testShippingSummary() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> shippingSummaryTest = TestSubscriber.create();
    vm.outputs.shippingSummary().subscribe(shippingSummaryTest);
    final TestSubscriber<Boolean> shippingSummaryIsHiddenTest = TestSubscriber.create();
    vm.outputs.shippingSummaryIsHidden().subscribe(shippingSummaryIsHiddenTest);

    // Reward with no shipping should hide shipping summary section and not emit a shipping summary string.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    shippingSummaryTest.assertNoValues();
    shippingSummaryIsHiddenTest.assertValue(true);

    // Reward with shipping should show shipping summary section and emit a shipping summary string.
    final Reward rewardWithShipping = RewardFactory.rewardWithShipping();
    vm.inputs.projectAndReward(project, rewardWithShipping);
    shippingSummaryTest.assertValue(rewardWithShipping.shippingSummary());
    shippingSummaryIsHiddenTest.assertValues(true, false);
  }

  @Test
  public void testUsdConversionNotShownForUSProject() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    final CurrentConfigType currentConfig = environment.currentConfig();
    environment.currentConfig().config(config);
    final RewardViewModel vm = new RewardViewModel(environment);

    // Set project's country to US.
    final Project project = ProjectFactory.project().toBuilder().country("US").build();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<String> usdConversion = TestSubscriber.create();
    vm.outputs.usdConversion().subscribe(usdConversion);
    final TestSubscriber<Boolean> usdConversionIsHidden = TestSubscriber.create();
    vm.outputs.usdConversionIsHidden().subscribe(usdConversionIsHidden);

    // USD conversion should not be shown.
    vm.inputs.projectAndReward(project, reward);
    usdConversion.assertNoValues();
    usdConversionIsHidden.assertValue(true);

    // Set user's country to CA.
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should still not be shown.
    usdConversion.assertNoValues();
    usdConversionIsHidden.assertValues(true, true);
  }

  @Test
  public void testUsdConversionForNonUSProject() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    final CurrentConfigType currentConfig = environment.currentConfig();
    environment.currentConfig().config(config);
    final RewardViewModel vm = new RewardViewModel(environment);

    // Set project's country to CA.
    final Project project = ProjectFactory.project().toBuilder().country("CA").build();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<String> usdConversion = TestSubscriber.create();
    vm.outputs.usdConversion().subscribe(usdConversion);
    final TestSubscriber<Boolean> usdConversionIsHidden = TestSubscriber.create();
    vm.outputs.usdConversionIsHidden().subscribe(usdConversionIsHidden);

    // USD conversion should be shown.
    vm.inputs.projectAndReward(project, reward);
    usdConversion.assertValueCount(1);
    usdConversionIsHidden.assertValue(false);

    // Set user's country to CA (any country except the US is fine).
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should now be hidden.
    usdConversion.assertValueCount(1);
    usdConversionIsHidden.assertValues(false, true);
  }
}
