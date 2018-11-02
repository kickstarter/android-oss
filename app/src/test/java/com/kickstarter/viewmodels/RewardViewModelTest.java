package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.ConfigFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import rx.observers.TestSubscriber;

import static java.util.Collections.emptyList;

public final class RewardViewModelTest extends KSRobolectricTestCase {
  private RewardViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> allGoneTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Integer> backersTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backersTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> conversionTextViewText = TestSubscriber.create();
  private final TestSubscriber<Boolean> conversionSectionIsGone = TestSubscriber.create();
  private final TestSubscriber<String> descriptionTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> estimatedDeliveryDateSectionIsGone = new TestSubscriber<>();
  private final TestSubscriber<DateTime> estimatedDeliveryDateTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> isClickable = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitAndBackersSeparatorIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitAndRemainingTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> limitAndRemainingTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitHeaderIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> minimumTextViewText= new TestSubscriber<>();
  private final TestSubscriber<Boolean> rewardDescriptionIsGone= new TestSubscriber<>();
  private final TestSubscriber<List<RewardsItem>> rewardsItemList= new TestSubscriber<>();
  private final TestSubscriber<Boolean> rewardsItemsAreGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> selectedHeaderIsGone = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shippingSummarySectionIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> shippingSummaryTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Project> startBackingActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Reward>> startCheckoutActivity = new TestSubscriber<>();
  private final TestSubscriber<Boolean> titleTextViewIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> titleTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> whiteOverlayIsInvisible = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new RewardViewModel.ViewModel(environment);
    this.vm.outputs.allGoneTextViewIsGone().subscribe(this.allGoneTextViewIsGone);
    this.vm.outputs.backersTextViewIsGone().subscribe(this.backersTextViewIsGone);
    this.vm.outputs.backersTextViewText().subscribe(this.backersTextViewText);
    this.vm.outputs.conversionTextViewText().subscribe(this.conversionTextViewText);
    this.vm.outputs.conversionTextViewIsGone().subscribe(this.conversionSectionIsGone);
    this.vm.outputs.descriptionTextViewText().subscribe(this.descriptionTextViewText);
    this.vm.outputs.estimatedDeliveryDateSectionIsGone().subscribe(this.estimatedDeliveryDateSectionIsGone);
    this.vm.outputs.estimatedDeliveryDateTextViewText().subscribe(this.estimatedDeliveryDateTextViewText);
    this.vm.outputs.isClickable().subscribe(this.isClickable);
    this.vm.outputs.limitAndBackersSeparatorIsGone().subscribe(this.limitAndBackersSeparatorIsGone);
    this.vm.outputs.limitAndRemainingTextViewText().subscribe(this.limitAndRemainingTextViewText);
    this.vm.outputs.limitAndRemainingTextViewIsGone().subscribe(this.limitAndRemainingTextViewIsGone);
    this.vm.outputs.limitHeaderIsGone().subscribe(this.limitHeaderIsGone);
    this.vm.outputs.minimumTextViewText().subscribe(this.minimumTextViewText);
    this.vm.outputs.rewardDescriptionIsGone().subscribe(this.rewardDescriptionIsGone);
    this.vm.outputs.rewardsItemsAreGone().subscribe(this.rewardsItemsAreGone);
    this.vm.outputs.rewardsItemList().subscribe(this.rewardsItemList);
    this.vm.outputs.selectedHeaderIsGone().subscribe(this.selectedHeaderIsGone);
    this.vm.outputs.shippingSummarySectionIsGone().subscribe(this.shippingSummarySectionIsGone);
    this.vm.outputs.shippingSummaryTextViewText().subscribe(this.shippingSummaryTextViewText);
    this.vm.outputs.startBackingActivity().subscribe(this.startBackingActivity);
    this.vm.outputs.startCheckoutActivity().subscribe(this.startCheckoutActivity);
    this.vm.outputs.titleTextViewIsGone().subscribe(this.titleTextViewIsGone);
    this.vm.outputs.titleTextViewText().subscribe(this.titleTextViewText);
    this.vm.outputs.whiteOverlayIsInvisible().subscribe(this.whiteOverlayIsInvisible);
  }

  @Test
  public void testAllGoneTextViewIsGone() {
    final Project project = ProjectFactory.backedProjectWithRewardLimitReached();
    setUpEnvironment(environment());

    // When an unlimited reward is not backed, hide the 'all gone' header.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.allGoneTextViewIsGone.assertValues(true);

    // When an unlimited reward is backed, hide the 'all gone' header (distinct until changed).
    final Reward backedReward = project.backing().reward();
    this.vm.inputs.projectAndReward(project, backedReward);
    this.allGoneTextViewIsGone.assertValues(true);

    // When a backed reward's limit has been reached, hide the 'all gone' header – the selected banner will be shown instead.
    final Reward backedRewardWithLimitReached = backedReward.toBuilder()
      .limit(1)
      .remaining(0)
      .build();
    this.vm.inputs.projectAndReward(project, backedRewardWithLimitReached);
    this.allGoneTextViewIsGone.assertValues(true);

    // When a reward's limit has been reached and it has not been backed, show the 'all gone' header.
    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.allGoneTextViewIsGone.assertValues(true, false);
  }

  @Test
  public void testBackersTextViewIsGone() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    this.backersTextViewIsGone.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.noReward());
    this.backersTextViewIsGone.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.backers());
    this.backersTextViewIsGone.assertValues(true, false);
  }

  @Test
  public void testBackersTextView() {
    final Project project = ProjectFactory.project();
    final Reward rewardWithBackers = RewardFactory.reward().toBuilder().backersCount(100).build();
    setUpEnvironment(environment());

    // Show reward backer count.
    this.vm.inputs.projectAndReward(project, rewardWithBackers);
    this.backersTextViewText.assertValue(100);
  }

  @Test
  public void testConversionHiddenForProject() {
    // Set the project currency and the user's chosen currency to the same value
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project().toBuilder().currency("USD").currentCurrency("USD").build();
    final Reward reward = RewardFactory.reward();

    // the conversion should be hidden.
    this.vm.inputs.projectAndReward(project, reward);
    this.conversionTextViewText.assertValueCount(1);
    this.conversionSectionIsGone.assertValue(true);
  }

  @Test
  public void testConversionShownForProject() {
    // Set the project currency and the user's chosen currency to different values
    setUpEnvironment(environment());
    final Project project = ProjectFactory.project().toBuilder().currency("CAD").currentCurrency("USD").build();
    final Reward reward = RewardFactory.reward();

    // USD conversion should shown.
    this.vm.inputs.projectAndReward(project, reward);
    this.conversionTextViewText.assertValueCount(1);
    this.conversionSectionIsGone.assertValue(false);
  }

  @Test
  public void testConversionTextRoundsUp() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    environment.currentConfig().config(config);
    setUpEnvironment(environment);

    // Set project's country to CA and reward minimum to $0.30.
    final Project project = ProjectFactory.caProject();
    final Reward reward = RewardFactory.reward().toBuilder().minimum(0.3f).build();

    // USD conversion should be rounded up.
    this.vm.inputs.projectAndReward(project, reward);
    this.conversionTextViewText.assertValue("$1");
  }

  @Test
  public void testDescriptionTextViewText() {
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.descriptionTextViewText.assertValue(reward.description());
  }

  @Test
  public void testEstimatedDeliveryDateTextViewText() {
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();
    setUpEnvironment(environment());


    this.vm.inputs.projectAndReward(project, reward);

    // If reward has no estimated delivery, no value should be emitted.
    this.estimatedDeliveryDateTextViewText.assertNoValues();

    // Reward with estimated delivery should emit.
    final DateTime estimatedDelivery = DateTime.now();
    this.vm.inputs.projectAndReward(project, reward.toBuilder().estimatedDeliveryOn(estimatedDelivery).build());

    this.estimatedDeliveryDateTextViewText.assertValue(estimatedDelivery);
  }

  @Test
  public void testEstimatedDeliveryDateSectionIsGone() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Reward with no estimated delivery should not show estimated delivery label.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(null).build());
    this.estimatedDeliveryDateSectionIsGone.assertValue(true);

    // Reward with estimated delivery should show estimated delivery label.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(DateTime.now()).build());
    this.estimatedDeliveryDateSectionIsGone.assertValues(true, false);
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessful() {
    final Project project = ProjectFactory.successfulProject();
    final Reward reward = RewardFactory.reward();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.startCheckoutActivity.assertNoValues();

    this.vm.inputs.rewardClicked();
    this.startCheckoutActivity.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
    final Project project = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    final Reward reward = project.backing().reward();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.startCheckoutActivity.assertNoValues();

    this.vm.inputs.rewardClicked();
    this.startCheckoutActivity.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsLive() {
    final Reward reward = RewardFactory.reward();
    final Project liveProject = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(liveProject, reward);
    this.startCheckoutActivity.assertNoValues();

    // When a reward from a live project is clicked, start checkout.
    this.vm.inputs.rewardClicked();
    this.startCheckoutActivity.assertValue(Pair.create(liveProject, reward));
  }

  @Test
  public void testGoToViewPledge() {
    final Project liveProject = ProjectFactory.backedProject();
    final Project successfulProject = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(liveProject, liveProject.backing().reward());
    this.startBackingActivity.assertNoValues();

    // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
    this.vm.inputs.rewardClicked();
    this.startBackingActivity.assertNoValues();

    // When project is successful but not backed, don't go to view pledge.
    this.vm.inputs.projectAndReward(successfulProject, RewardFactory.reward());
    this.vm.inputs.rewardClicked();
    this.startBackingActivity.assertNoValues();

    // When project is successful and backed, go to view pledge.
    this.vm.inputs.projectAndReward(successfulProject, successfulProject.backing().reward());
    this.startBackingActivity.assertNoValues();
    this.vm.inputs.rewardClicked();
    this.startBackingActivity.assertValues(successfulProject);
  }

  @Test
  public void testIsClickable() {
    setUpEnvironment(environment());

    // A reward from a live project should be clickable.
    this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    this.isClickable.assertValue(true);

    // A reward from a successful project should not be clickable.
    this.vm.inputs.projectAndReward(ProjectFactory.successfulProject(), RewardFactory.reward());
    this.isClickable.assertValues(true, false);
    //
    // A backed reward from a live project should be clickable.
    final Project backedLiveProject = ProjectFactory.backedProject();
    this.vm.inputs.projectAndReward(backedLiveProject, backedLiveProject.backing().reward());
    this.isClickable.assertValues(true, false, true);

    // A backed reward from a finished project should be clickable (distinct until changed).
    final Project backedSuccessfulProject = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    this.vm.inputs.projectAndReward(backedSuccessfulProject, backedSuccessfulProject.backing().reward());
    this.isClickable.assertValues(true, false, true);

    // A reward with its limit reached should not be clickable.
    this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.limitReached());
    this.isClickable.assertValues(true, false, true, false);
  }

  @Test
  public void testLimitAndBackersSeparatorIsGone() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // When reward has no limit or backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    this.limitAndBackersSeparatorIsGone.assertValues(true);

    // When reward has no limit and backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.limitAndBackersSeparatorIsGone.assertValues(true);

    // When reward has limit and no backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().backersCount(0).build());
    this.limitAndBackersSeparatorIsGone.assertValues(true);

    // When reward has limit and backers, separator should be visible.
    this.vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().build());
    this.limitAndBackersSeparatorIsGone.assertValues(true, false);
  }

  @Test
  public void testLimitAndRemaining() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // When reward is limited, quantity should be shown.
    final Reward limitedReward = RewardFactory.reward().toBuilder()
      .limit(10)
      .remaining(5)
      .build();
    this.vm.inputs.projectAndReward(project, limitedReward);
    this.limitAndRemainingTextViewText.assertValue(Pair.create("10", "5"));
    this.limitAndRemainingTextViewIsGone.assertValue(false);

    // When reward's limit has been reached, don't show quantity.
    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.limitAndRemainingTextViewIsGone.assertValues(false, true);

    // When reward has no limit, don't show quantity (distinct until changed).
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.limitAndRemainingTextViewIsGone.assertValues(false, true);
  }

  @Test
  public void testLimitHeaderIsGone() {
    setUpEnvironment(environment());

    // If the reward is limited and has not been backed, show the limit header.
    final Project backedProjectWithLimitedReward = ProjectFactory.backedProjectWithRewardLimited();
    this.vm.inputs.projectAndReward(backedProjectWithLimitedReward, RewardFactory.limited());
    this.limitHeaderIsGone.assertValues(false);

    // If the reward is limited and has been backed, don't show the limit header.
    this.vm.inputs.projectAndReward(backedProjectWithLimitedReward, backedProjectWithLimitedReward.backing().reward());
    this.limitHeaderIsGone.assertValues(false, true);

    // If the reward is not limited, don't show the limit header.
    this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    this.limitHeaderIsGone.assertValues(false, true);
  }

  @Test
  public void testMinimumTextViewText() {
    final Project project = ProjectFactory.project();
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(10)
      .build();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.minimumTextViewText.assertValue("$10");
  }

  @Test
  public void testRewardsItems() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Items section should be hidden when there are no items.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.rewardsItemsAreGone.assertValue(true);
    this.rewardsItemList.assertValue(emptyList());

    final Reward itemizedReward = RewardFactory.itemized();
    this.vm.inputs.projectAndReward(project, itemizedReward);
    this.rewardsItemsAreGone.assertValues(true, false);
    this.rewardsItemList.assertValues(emptyList(), itemizedReward.rewardsItems());
  }

  @Test
  public void testTitleTextViewText() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Reward with no title should be hidden.
    final Reward rewardWithNoTitle = RewardFactory.reward().toBuilder()
      .title(null)
      .build();
    this.vm.inputs.projectAndReward(project, rewardWithNoTitle);
    this.titleTextViewIsGone.assertValues(true);
    this.titleTextViewText.assertNoValues();

    // Reward with title should be visible.
    final String title = "Digital bundle";
    final Reward rewardWithTitle = RewardFactory.reward().toBuilder()
      .title(title)
      .build();
    this.vm.inputs.projectAndReward(project, rewardWithTitle);
    this.titleTextViewIsGone.assertValues(true, false);
    this.titleTextViewText.assertValue(title);
  }

  @Test
  public void testSelectedHeaderAndOverlay() {
    final Project backedProject = ProjectFactory.backedProject();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(backedProject, backedProject.backing().reward());
    this.selectedHeaderIsGone.assertValue(false);

    this.vm.inputs.projectAndReward(backedProject, RewardFactory.reward());
    this.selectedHeaderIsGone.assertValues(false, true);
  }

  @Test
  public void testShippingSummary() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Reward with no shipping should hide shipping summary section and not emit a shipping summary string.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.shippingSummaryTextViewText.assertNoValues();
    this.shippingSummarySectionIsGone.assertValue(true);

    // Reward with shipping should show shipping summary section and emit a shipping summary string.
    final Reward rewardWithShipping = RewardFactory.rewardWithShipping();
    this.vm.inputs.projectAndReward(project, rewardWithShipping);
    this.shippingSummaryTextViewText.assertValue(rewardWithShipping.shippingSummary());
    this.shippingSummarySectionIsGone.assertValues(true, false);
  }

  @Test
  public void testWhiteOverlayIsHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.whiteOverlayIsInvisible.assertValue(true);

    final Project backedProjectWithRewardLimitReached = ProjectFactory.backedProjectWithRewardLimitReached();
    this.vm.inputs.projectAndReward(backedProjectWithRewardLimitReached, backedProjectWithRewardLimitReached.backing().reward());
    this.whiteOverlayIsInvisible.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.whiteOverlayIsInvisible.assertValues(true, false);
  }

  @Test
  public void testNonEmptyRewardsDescriptionAreShown() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.rewardDescriptionIsGone.assertValue(false);
  }

  @Test
  public void testEmptyRewardsDescriptionAreGone() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.noDescription());
    this.rewardDescriptionIsGone.assertValue(true);
  }
}
