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
  public void testAllGoneHeaderIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.backedProjectWithRewardLimitReached();

    final TestSubscriber<Boolean> allGoneHeaderIsHidden = TestSubscriber.create();
    vm.outputs.allGoneHeaderIsHidden().subscribe(allGoneHeaderIsHidden);

    // When an unlimited reward is not backed, hide the 'all gone' header.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    allGoneHeaderIsHidden.assertValues(true);

    // When an unlimited reward is backed, hide the 'all gone' header (distinct until changed).
    final Reward backedReward = project.backing().reward();
    vm.inputs.projectAndReward(project, backedReward);
    allGoneHeaderIsHidden.assertValues(true);

    // When a backed reward's limit has been reached, hide the 'all gone' header – the selected banner will be shown instead.
    final Reward backedRewardWithLimitReached = backedReward.toBuilder()
      .limit(1)
      .remaining(0)
      .build();
    vm.inputs.projectAndReward(project, backedRewardWithLimitReached);
    allGoneHeaderIsHidden.assertValues(true);

    // When a reward's limit has been reached and it has not been backed, show the 'all gone' header.
    vm.inputs.projectAndReward(project, RewardFactory.rewardWithLimitReached());
    allGoneHeaderIsHidden.assertValues(true, false);
  }

  @Test
  public void testBackersTextView() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward rewardWithBackers = RewardFactory.reward().toBuilder()
      .backersCount(10)
      .build();

    final TestSubscriber<Integer> backersTextViewTextTest = TestSubscriber.create();
    vm.outputs.backersTextViewText().subscribe(backersTextViewTextTest);

    final TestSubscriber<Boolean> backersTextViewIsHiddenTest = TestSubscriber.create();
    vm.outputs.backersTextViewIsHidden().subscribe(backersTextViewIsHiddenTest);

    // Show reward backer count.
    vm.inputs.projectAndReward(project, rewardWithBackers);

    backersTextViewTextTest.assertValue(10);
    backersTextViewIsHiddenTest.assertValue(false);

    // If reward is 'no reward', backers should be hidden.
    vm.inputs.projectAndReward(project, RewardFactory.noReward());
    backersTextViewTextTest.assertValues(10);
    backersTextViewIsHiddenTest.assertValues(false, true);
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
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.rewardWithLimitReached());
    isClickableTest.assertValues(true, false, true, false);
  }

  @Test
  public void testLimitDividerIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> limitDividerIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitDividerIsHidden().subscribe(limitDividerIsHiddenTest);

    // Time limit is not implemented in backend, just assert that the divider is not shown for now.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    limitDividerIsHiddenTest.assertValue(true);
  }

  @Test
  public void testLimitAndRemaining() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Pair<String, String>> limitAndRemainingTextViewTextTest = TestSubscriber.create();
    vm.outputs.limitAndRemainingTextViewText().subscribe(limitAndRemainingTextViewTextTest);
    final TestSubscriber<Boolean> limitAndRemainingSectionIsHidden = TestSubscriber.create();
    vm.outputs.limitAndRemainingSectionIsHidden().subscribe(limitAndRemainingSectionIsHidden);

    // When reward is limited, quantity should be shown.
    final Reward limitedReward = RewardFactory.reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
    vm.inputs.projectAndReward(project, limitedReward);
    limitAndRemainingTextViewTextTest.assertValue(Pair.create("10", "5"));
    limitAndRemainingSectionIsHidden.assertValue(false);

    // When reward's limit has been reached, don't show quantity.
    vm.inputs.projectAndReward(project, RewardFactory.rewardWithLimitReached());
    limitAndRemainingSectionIsHidden.assertValues(false, true);

    // When reward has no limit, don't show quantity (distinct until changed).
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    limitAndRemainingSectionIsHidden.assertValues(false, true);
  }

  @Test
  public void testLimitAndRemainingSectionIsCenterAligned() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> limitAndRemainingSectionIsCenterAligned = TestSubscriber.create();
    vm.outputs.limitAndRemainingSectionIsCenterAligned().subscribe(limitAndRemainingSectionIsCenterAligned);

    // Time limit is not implemented yet, so just always default this to false.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    limitAndRemainingSectionIsCenterAligned.assertValue(false);
  }

  @Test
  public void testLimitHeaderIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<Boolean> limitHeaderIsHiddenTest = TestSubscriber.create();
    vm.outputs.limitHeaderIsHidden().subscribe(limitHeaderIsHiddenTest);

    // If the reward is limited and has not been backed, show the limit header.
    final Project backedProjectWithLimitedReward = ProjectFactory.backedProjectWithRewardLimited();
    vm.inputs.projectAndReward(backedProjectWithLimitedReward, RewardFactory.limitedReward());
    limitHeaderIsHiddenTest.assertValues(false);

    // If the reward is limited and has been backed, don't show the limit header.
    vm.inputs.projectAndReward(backedProjectWithLimitedReward, backedProjectWithLimitedReward.backing().reward());
    limitHeaderIsHiddenTest.assertValues(false, true);

    // If the reward is not limited, don't show the limit header.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    limitHeaderIsHiddenTest.assertValues(false, true);
  }

  @Test
  public void testMinimumButtonIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> minimumButtonIsHiddenTest = TestSubscriber.create();
    vm.outputs.minimumButtonIsHidden().subscribe(minimumButtonIsHiddenTest);

    // When reward is unlimited, show minimum button.
    vm.inputs.projectAndReward(project, RewardFactory.reward());
    minimumButtonIsHiddenTest.assertValue(false);

    // When reward's limit has been reached, hide minimum button.
    vm.inputs.projectAndReward(project, RewardFactory.rewardWithLimitReached());
    minimumButtonIsHiddenTest.assertValues(false, true);

    // If reward has no title, hide minimum button (distinct until changed).
    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .title(null)
      .build();
    vm.inputs.projectAndReward(project, rewardWithNoTitle);
    minimumButtonIsHiddenTest.assertValues(false, true);
  }

  @Test
  public void testMinimumButtonText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(10)
      .build();

    final TestSubscriber<String> minimumButtonText = TestSubscriber.create();
    vm.outputs.minimumButtonText().subscribe(minimumButtonText);

    vm.inputs.projectAndReward(project, reward);

    minimumButtonText.assertValue("$10");
  }

  @Test
  public void testMinimumTextViewIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project liveProject = ProjectFactory.project();
    final Project successfulProject = ProjectFactory.successfulProject();

    final TestSubscriber<Boolean> minimumTextViewIsHiddenTest = TestSubscriber.create();
    vm.outputs.minimumTextViewIsHidden().subscribe(minimumTextViewIsHiddenTest);

    // For rewards with no limit and a live project, hide minimum text view.
    vm.inputs.projectAndReward(liveProject, RewardFactory.reward());
    minimumTextViewIsHiddenTest.assertValue(true);

    // For rewards with no limit and a finished project, show minimum text view.
    vm.inputs.projectAndReward(successfulProject, RewardFactory.reward());
    minimumTextViewIsHiddenTest.assertValues(true, false);

    // If reward has no title, hide minimum text view.
    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .title(null)
      .build();
    vm.inputs.projectAndReward(successfulProject, rewardWithNoTitle);
    minimumTextViewIsHiddenTest.assertValues(true, false, true);

    // If reward's limit has been reached, show minimum text view.
    final Reward rewardWithLimitReached = RewardFactory.rewardWithLimitReached();
    vm.inputs.projectAndReward(liveProject, rewardWithLimitReached);
    minimumTextViewIsHiddenTest.assertValues(true, false, true, false);
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
  public void testMinimumTitleTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> minimumTitleTextViewTest = TestSubscriber.create();
    vm.outputs.minimumTitleTextViewText().subscribe(minimumTitleTextViewTest);

    // Minimum title is only used when the reward has no title.
    final Reward rewardWithTitle = RewardFactory.reward().toBuilder()
      .title("Digital bundle")
      .build();
    vm.inputs.projectAndReward(project, rewardWithTitle);
    minimumTitleTextViewTest.assertNoValues();

    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .minimum(10.0f)
      .title(null)
      .build();
    vm.inputs.projectAndReward(project, rewardWithNoTitle);
    minimumTitleTextViewTest.assertValues("$10");
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

    final Reward itemizedReward = RewardFactory.itemizedReward();
    vm.inputs.projectAndReward(project, itemizedReward);
    rewardsItemsAreHiddenTest.assertValues(true, false);
    rewardsItemsTest.assertValues(emptyList(), itemizedReward.rewardsItems());
  }

  @Test
  public void testRewardTitleTextViewText() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<String> rewardTitleTextViewTest = TestSubscriber.create();
    vm.outputs.rewardTitleTextViewText().subscribe(rewardTitleTextViewTest);

    // Reward with no title should be hidden.
    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .title(null)
      .build();
    vm.inputs.projectAndReward(project, rewardWithNoTitle);
    rewardTitleTextViewTest.assertNoValues();

    // Reward with title should be visible.
    final String title = "Digital bundle";
    final Reward rewardWithTitle = RewardFactory.reward().toBuilder()
      .title(title)
      .build();
    vm.inputs.projectAndReward(project, rewardWithTitle);
    rewardTitleTextViewTest.assertValue(title);
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
  public void testTimeLimit() {
    final RewardViewModel vm = new RewardViewModel(environment());

    final TestSubscriber<String> timeLimitTextViewTextTest = TestSubscriber.create();
    vm.outputs.timeLimitTextViewText().subscribe(timeLimitTextViewTextTest);
    final TestSubscriber<Boolean> timeLimitSectionIsHiddenTest = TestSubscriber.create();
    vm.outputs.timeLimitSectionIsHidden().subscribe(timeLimitSectionIsHiddenTest);

    // Not implemented in backend, just assert that this isn't shown for now.
    vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    timeLimitTextViewTextTest.assertNoValues();
    timeLimitSectionIsHiddenTest.assertValue(true);
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
  public void testWhiteOverlayIsHidden() {
    final RewardViewModel vm = new RewardViewModel(environment());
    final Project project = ProjectFactory.project();

    final TestSubscriber<Boolean> whiteOverlayIsHiddenTest = TestSubscriber.create();
    vm.outputs.whiteOverlayIsHidden().subscribe(whiteOverlayIsHiddenTest);

    vm.inputs.projectAndReward(project, RewardFactory.reward());
    whiteOverlayIsHiddenTest.assertValue(true);

    vm.inputs.projectAndReward(project, RewardFactory.rewardWithLimitReached());
    whiteOverlayIsHiddenTest.assertValues(true, false);
  }
}
