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
import com.kickstarter.models.RewardsItem;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

import static java.util.Collections.emptyList;

public final class RewardViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testAllGoneTextViewIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.backedProjectWithRewardLimitReached();

    final TestSubscriber<Boolean> allGoneTextViewIsHidden = TestSubscriber.create();
    vm.outputs.allGoneTextViewIsHidden().subscribe(allGoneTextViewIsHidden);

    // When an unlimited reward is not backed, hide the 'all gone' header.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    allGoneTextViewIsHidden.assertValues(true);

    // When an unlimited reward is backed, hide the 'all gone' header (distinct until changed).
    final Reward backedReward = project.backing().reward();
    vm.inputs.projectAndReward(project, backedReward);
    allGoneTextViewIsHidden.assertValues(true);

    // When a backed reward's limit has been reached, hide the 'all gone' header – the selected banner will be shown instead.
    final Reward backedRewardWithLimitReached = backedReward.toBuilder()
      .limit(1)
      .remaining(0)
      .build();
    vm.inputs.projectAndReward(project, backedRewardWithLimitReached);
    allGoneTextViewIsHidden.assertValues(true);

    // When a reward's limit has been reached and it has not been backed, show the 'all gone' header.
    vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    allGoneTextViewIsHidden.assertValues(true, false);
  }

  @Test
  public void testBackersTextViewIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> backersTextViewIsHiddenTest = TestSubscriber.create();
    vm.outputs.backersTextViewIsHidden().subscribe(backersTextViewIsHiddenTest);

    vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    backersTextViewIsHiddenTest.assertValues(true);

    vm.inputs.projectAndReward(project, RewardFactory.noReward());
    backersTextViewIsHiddenTest.assertValues(true);

    vm.inputs.projectAndReward(project, RewardFactory.backers());
    backersTextViewIsHiddenTest.assertValues(true, false);
  }

  @Test
  public void testBackersTextView() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward rewardWithBackers = RewardFactory.reward().toBuilder().backersCount(100).build();

    final TestSubscriber<Integer> backersTextViewTextTest = TestSubscriber.create();
    vm.outputs.backersTextViewText().subscribe(backersTextViewTextTest);

    // Show reward backer count.
    vm.inputs.projectAndReward(project, rewardWithBackers);

    backersTextViewTextTest.assertValue(100);
  }

  @Test
  public void testDescriptionTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<String> descriptionTextViewTextTest = TestSubscriber.create();
    vm.outputs.descriptionTextViewText().subscribe(descriptionTextViewTextTest);

    vm.inputs.projectAndReward(project, reward);

    descriptionTextViewTextTest.assertValue(reward.description());
  }

  @Test
  public void testEstimatedDeliveryDateTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();

    final TestSubscriber<DateTime> estimatedDeliveryDateTextViewTextTest = TestSubscriber.create();
    vm.outputs.estimatedDeliveryDateTextViewText().subscribe(estimatedDeliveryDateTextViewTextTest);

    vm.inputs.projectAndReward(project, reward);

    // If reward has no estimated delivery, no value should be emitted.
    estimatedDeliveryDateTextViewTextTest.assertNoValues();

    // Reward with estimated delivery should emit.
    final DateTime estimatedDelivery = DateTime.now();
    vm.inputs.projectAndReward(project, reward.toBuilder().estimatedDeliveryOn(estimatedDelivery).build());

    estimatedDeliveryDateTextViewTextTest.assertValue(estimatedDelivery);
  }

  @Test
  public void testEstimatedDeliveryDateSectionIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> estimatedDeliveryDateSectionIsHiddenTest = TestSubscriber.create();
    vm.outputs.estimatedDeliveryDateSectionIsHidden().subscribe(estimatedDeliveryDateSectionIsHiddenTest);

    // Reward with no estimated delivery should not show estimated delivery label.
    vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(null).build());
    estimatedDeliveryDateSectionIsHiddenTest.assertValue(true);

    // Reward with estimated delivery should show estimated delivery label.
    vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(DateTime.now()).build());
    estimatedDeliveryDateSectionIsHiddenTest.assertValues(true, false);
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessful() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.successfulProject();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<Pair<Project, Reward>> goToCheckoutTest = TestSubscriber.create();
    vm.outputs.goToCheckout().subscribe(goToCheckoutTest);

    vm.inputs.projectAndReward(project, reward);
    goToCheckoutTest.assertNoValues();

    vm.inputs.rewardClicked();
    goToCheckoutTest.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    final Reward reward = project.backing().reward();

    final TestSubscriber<Pair<Project, Reward>> goToCheckoutTest = TestSubscriber.create();
    vm.outputs.goToCheckout().subscribe(goToCheckoutTest);

    vm.inputs.projectAndReward(project, reward);
    goToCheckoutTest.assertNoValues();

    vm.inputs.rewardClicked();
    goToCheckoutTest.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsLive() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<Pair<Project, Reward>> startCheckoutTest = TestSubscriber.create();
    vm.outputs.goToCheckout().subscribe(startCheckoutTest);

    final Project liveProject = ProjectFactory.project();
    vm.inputs.projectAndReward(liveProject, reward);
    startCheckoutTest.assertNoValues();

    // When a reward from a live project is clicked, start checkout.
    vm.inputs.rewardClicked();
    startCheckoutTest.assertValue(Pair.create(liveProject, reward));
  }

  @Test
  public void testGoToViewPledge() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project liveProject = ProjectFactory.backedProject();
    final Project successfulProject = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();

    final TestSubscriber<Project> goToViewPledgeTest = TestSubscriber.create();
    vm.outputs.goToViewPledge().subscribe(goToViewPledgeTest);

    vm.inputs.projectAndReward(liveProject, liveProject.backing().reward());
    goToViewPledgeTest.assertNoValues();

    // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
    vm.inputs.rewardClicked();
    goToViewPledgeTest.assertNoValues();

    // When project is successful but not backed, don't go to view pledge.
    vm.inputs.projectAndReward(successfulProject, RewardFactory.reward());
    vm.inputs.rewardClicked();
    goToViewPledgeTest.assertNoValues();

    // When project is successful and backed, go to view pledge.
    vm.inputs.projectAndReward(successfulProject, successfulProject.backing().reward());
    goToViewPledgeTest.assertNoValues();
    vm.inputs.rewardClicked();
    goToViewPledgeTest.assertValues(successfulProject);
  }

  @Test
  public void testIsClickable() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> isClickableTest = TestSubscriber.create();
    vm.outputs.isClickable().subscribe(isClickableTest);

    // A reward from a live project should be clickable.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    isClickableTest.assertValue(true);

    // A reward from a successful project should not be clickable.
    vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward());
    isClickableTest.assertValues(true, false);
    //
    // A backed reward from a live project should be clickable.
    final Project backedLiveProject = ProjectFactory.backedProject();
    vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing().reward());
    isClickableTest.assertValues(true, false, true);

    // A backed reward from a finished project should be clickable (distinct until changed).
    final Project backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing().reward());
    isClickableTest.assertValues(true, false, true);

    // A reward with its limit reached should not be clickable.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.limitReached());
    isClickableTest.assertValues(true, false, true, false);
  }

  @Test
  public void testLimitAndBackersSeparatorIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> limitAndBackersSeparatorIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitAndBackersSeparatorIsHidden().subscribe(limitAndBackersSeparatorIsHiddenTest);

    // When reward has no limit or backers, separator should be hidden.
    vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    limitAndBackersSeparatorIsHiddenTest.assertValues(true);

    // When reward has no limit and backers, separator should be hidden.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    limitAndBackersSeparatorIsHiddenTest.assertValues(true);

    // When reward has limit and no backers, separator should be hidden.
    vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().backersCount(0).build());
    limitAndBackersSeparatorIsHiddenTest.assertValues(true);

    // When reward has limit and backers, separator should be visible.
    vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().build());
    limitAndBackersSeparatorIsHiddenTest.assertValues(true, false);
  }

  @Test
  public void testLimitAndRemaining() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Pair<String, String>> limitAndRemainingTextViewTextTest = TestSubscriber.create();
    vm.outputs.limitAndRemainingTextViewText().subscribe(limitAndRemainingTextViewTextTest);
    final TestSubscriber<Boolean> limitAndRemainingTextViewIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitAndRemainingTextViewIsHidden().subscribe(limitAndRemainingTextViewIsHiddenTest);

    // When reward is limited, quantity should be shown.
    final Reward limitedReward = RewardFactory.reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
    vm.inputs.projectAndReward(project, limitedReward);
    limitAndRemainingTextViewTextTest.assertValue(Pair.create("10", "5"));
    limitAndRemainingTextViewIsHiddenTest.assertValue(false);

    // When reward's limit has been reached, don't show quantity.
    vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    limitAndRemainingTextViewIsHiddenTest.assertValues(false, true);

    // When reward has no limit, don't show quantity (distinct until changed).
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    limitAndRemainingTextViewIsHiddenTest.assertValues(false, true);
  }

  @Test
  public void testLimitHeaderIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> limitHeaderIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitHeaderIsHidden().subscribe(limitHeaderIsHiddenTest);

    // If the reward is limited and has not been backed, show the limit header.
    final Project backedProjectWithLimitedReward = ProjectFactory.backedProjectWithRewardLimited();
    vm.inputs.projectAndReward(backedProjectWithLimitedReward, RewardFactory.limited());
    limitHeaderIsHiddenTest.assertValues(false);

    // If the reward is limited and has been backed, don't show the limit header.
    vm.inputs.projectAndReward(backedProjectWithLimitedReward, backedProjectWithLimitedReward.backing().reward());
    limitHeaderIsHiddenTest.assertValues(false, true);

    // If the reward is not limited, don't show the limit header.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    limitHeaderIsHiddenTest.assertValues(false, true);
  }

  @Test
  public void testMinimumTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(10)
      .build();

    final TestSubscriber<String> minimumTextViewTextTest = TestSubscriber.create();
    vm.outputs.minimumTextViewText().subscribe(minimumTextViewTextTest);

    vm.inputs.projectAndReward(project, reward);

    minimumTextViewTextTest.assertValue("$10");
  }

  @Test
  public void testRewardsItems() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> rewardsItemsAreHiddenTest = TestSubscriber.create();
    vm.outputs.rewardsItemsAreHidden().subscribe(rewardsItemsAreHiddenTest);
    final TestSubscriber<List<RewardsItem>> rewardsItemsTest = TestSubscriber.create();
    vm.outputs.rewardsItems().subscribe(rewardsItemsTest);

    // Items section should be hidden when there are no items.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    rewardsItemsAreHiddenTest.assertValue(true);
    rewardsItemsTest.assertValue(emptyList());

    final Reward itemizedReward = RewardFactory.itemized();
    vm.inputs.projectAndReward(project, itemizedReward);
    rewardsItemsAreHiddenTest.assertValues(true, false);
    rewardsItemsTest.assertValues(emptyList(), itemizedReward.rewardsItems());
  }

  @Test
  public void testTitleTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> titleTextViewIsHidden = TestSubscriber.create();
    vm.outputs.titleTextViewIsHidden().subscribe(titleTextViewIsHidden);
    final TestSubscriber<String> titleTextViewTextTest = TestSubscriber.create();
    vm.outputs.titleTextViewText().subscribe(titleTextViewTextTest);

    // Reward with no title should be hidden.
    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .title(null)
      .build();
    vm.inputs.projectAndReward(project, rewardWithNoTitle);
    titleTextViewIsHidden.assertValues(true);
    titleTextViewTextTest.assertNoValues();

    // Reward with title should be visible.
    final String title = "Digital bundle";
    final Reward rewardWithTitle = RewardFactory.reward().toBuilder()
      .title(title)
      .build();
    vm.inputs.projectAndReward(project, rewardWithTitle);
    titleTextViewIsHidden.assertValues(true, false);
    titleTextViewTextTest.assertValue(title);
  }

  @Test
  public void testSelectedHeaderAndOverlay() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> selectedHeaderIsHidden = TestSubscriber.create();
    vm.outputs.selectedHeaderIsHidden().subscribe(selectedHeaderIsHidden);
    final TestSubscriber<Boolean> selectedOverlayIsHidden = TestSubscriber.create();
    vm.outputs.selectedOverlayIsHidden().subscribe(selectedOverlayIsHidden);

    final Project backedProject = ProjectFactory.backedProject();
    vm.inputs.projectAndReward(backedProject, backedProject.backing().reward());
    selectedHeaderIsHidden.assertValue(false);
    selectedOverlayIsHidden.assertValue(false);

    vm.inputs.projectAndReward(backedProject, RewardFactory.reward());
    selectedHeaderIsHidden.assertValues(false, true);
    selectedOverlayIsHidden.assertValues(false, true);
  }

  @Test
  public void testShippingSummary() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> shippingSummaryTextViewTextTest = TestSubscriber.create();
    vm.outputs.shippingSummaryTextViewText().subscribe(shippingSummaryTextViewTextTest);
    final TestSubscriber<Boolean> shippingSummarySectionIsHiddenTest = TestSubscriber.create();
    vm.outputs.shippingSummarySectionIsHidden().subscribe(shippingSummarySectionIsHiddenTest);

    // Reward with no shipping should hide shipping summary section and not emit a shipping summary string.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    shippingSummaryTextViewTextTest.assertNoValues();
    shippingSummarySectionIsHiddenTest.assertValue(true);

    // Reward with shipping should show shipping summary section and emit a shipping summary string.
    final Reward rewardWithShipping = RewardFactory.rewardWithShipping();
    vm.inputs.projectAndReward(project, rewardWithShipping);
    shippingSummaryTextViewTextTest.assertValue(rewardWithShipping.shippingSummary());
    shippingSummarySectionIsHiddenTest.assertValues(true, false);
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
    final Project project = ProjectFactory.caProject();
    final Reward reward = RewardFactory.reward();

    final TestSubscriber<String> usdConversionTextViewText = TestSubscriber.create();
    vm.outputs.usdConversionTextViewText().subscribe(usdConversionTextViewText);
    final TestSubscriber<Boolean> usdConversionSectionIsHidden = TestSubscriber.create();
    vm.outputs.usdConversionTextViewIsHidden().subscribe(usdConversionSectionIsHidden);

    // USD conversion should be shown.
    vm.inputs.projectAndReward(project, reward);
    usdConversionTextViewText.assertValueCount(1);
    usdConversionSectionIsHidden.assertValue(false);

    // Set user's country to CA (any country except the US is fine).
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should now be hidden.
    usdConversionTextViewText.assertValueCount(1);
    usdConversionSectionIsHidden.assertValues(false, true);
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

    final TestSubscriber<String> usdConversionTextViewText = TestSubscriber.create();
    vm.outputs.usdConversionTextViewText().subscribe(usdConversionTextViewText);
    final TestSubscriber<Boolean> usdConversionSectionIsHidden = TestSubscriber.create();
    vm.outputs.usdConversionTextViewIsHidden().subscribe(usdConversionSectionIsHidden);

    // USD conversion should not be shown.
    vm.inputs.projectAndReward(project, reward);
    usdConversionTextViewText.assertNoValues();
    usdConversionSectionIsHidden.assertValue(true);

    // Set user's country to CA.
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should still not be shown (distinct until changed).
    usdConversionTextViewText.assertNoValues();
    usdConversionSectionIsHidden.assertValues(true);
  }

  @Test
  public void testUsdConversionTextRoundsUp() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    environment.currentConfig().config(config);
    final RewardViewModel vm = new RewardViewModel(environment);

    // Set project's country to CA and reward minimum to $0.30.
    final Project project = ProjectFactory.caProject();
    final Reward reward = RewardFactory.reward().toBuilder().minimum(0.3f).build();

    final TestSubscriber<String> usdConversionTextViewText = TestSubscriber.create();
    vm.outputs.usdConversionTextViewText().subscribe(usdConversionTextViewText);

    // USD conversion should be rounded up.
    vm.inputs.projectAndReward(project, reward);
    usdConversionTextViewText.assertValue("$1");
  }

  @Test
  public void testWhiteOverlayIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> whiteOverlayIsHiddenTest = TestSubscriber.create();
    vm.outputs.whiteOverlayIsHidden().subscribe(whiteOverlayIsHiddenTest);

    vm.inputs.projectAndReward(project, RewardFactory.reward());
    whiteOverlayIsHiddenTest.assertValue(true);

    final Project backedProjectWithRewardLimitReached = ProjectFactory.backedProjectWithRewardLimitReached();
    vm.inputs.projectAndReward(backedProjectWithRewardLimitReached, backedProjectWithRewardLimitReached.backing().reward());
    whiteOverlayIsHiddenTest.assertValues(true);

    vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    whiteOverlayIsHiddenTest.assertValues(true, false);
  }
}
