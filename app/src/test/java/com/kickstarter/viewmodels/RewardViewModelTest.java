package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
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
  private RewardViewModel vm;
  private final TestSubscriber<Boolean> allGoneTextViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Integer> backersTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> backersTextViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> descriptionTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> estimatedDeliveryDateSectionIsHidden = new TestSubscriber<>();
  private final TestSubscriber<DateTime> estimatedDeliveryDateTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Reward>> goToCheckout = new TestSubscriber<>();
  private final TestSubscriber<Project> goToViewPledge = new TestSubscriber<>();
  private final TestSubscriber<Boolean> isClickable = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitAndBackersSeparatorIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitAndRemainingTextViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> limitAndRemainingTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> limitHeaderIsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> minimumTextViewText= new TestSubscriber<>();
  private final TestSubscriber<Boolean> rewardDescriptionIsHidden= new TestSubscriber<>();
  private final TestSubscriber<List<RewardsItem>> rewardsItemList= new TestSubscriber<>();
  private final TestSubscriber<Boolean> rewardsItemsAreHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> selectedHeaderIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> selectedOverlayIsHidden = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shippingSummarySectionIsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> shippingSummaryTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> titleTextViewIsHidden = new TestSubscriber<>();
  private final TestSubscriber<String> titleTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> usdConversionTextViewText = TestSubscriber.create();
  private final TestSubscriber<Boolean> usdConversionSectionIsHidden = TestSubscriber.create();
  private final TestSubscriber<Boolean> whiteOverlayIsHidden = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new RewardViewModel(environment);
    this.vm.outputs.allGoneTextViewIsHidden().subscribe(this.allGoneTextViewIsHidden);
    this.vm.outputs.backersTextViewIsHidden().subscribe(this.backersTextViewIsHidden);
    this.vm.outputs.backersTextViewText().subscribe(this.backersTextViewText);
    this.vm.outputs.descriptionTextViewText().subscribe(this.descriptionTextViewText);
    this.vm.outputs.estimatedDeliveryDateSectionIsHidden().subscribe(this.estimatedDeliveryDateSectionIsHidden);
    this.vm.outputs.estimatedDeliveryDateTextViewText().subscribe(this.estimatedDeliveryDateTextViewText);
    this.vm.outputs.goToCheckout().subscribe(this.goToCheckout);
    this.vm.outputs.goToViewPledge().subscribe(this.goToViewPledge);
    this.vm.outputs.isClickable().subscribe(this.isClickable);
    this.vm.outputs.limitAndBackersSeparatorIsHidden().subscribe(this.limitAndBackersSeparatorIsHidden);
    this.vm.outputs.limitAndRemainingTextViewText().subscribe(this.limitAndRemainingTextViewText);
    this.vm.outputs.limitAndRemainingTextViewIsHidden().subscribe(this.limitAndRemainingTextViewIsHidden);
    this.vm.outputs.limitHeaderIsHidden().subscribe(this.limitHeaderIsHidden);
    this.vm.outputs.minimumTextViewText().subscribe(this.minimumTextViewText);
    this.vm.outputs.rewardDescriptionIsHidden().subscribe(this.rewardDescriptionIsHidden);
    this.vm.outputs.rewardsItemsAreHidden().subscribe(this.rewardsItemsAreHidden);
    this.vm.outputs.rewardsItemList().subscribe(this.rewardsItemList);
    this.vm.outputs.selectedHeaderIsHidden().subscribe(this.selectedHeaderIsHidden);
    this.vm.outputs.selectedOverlayIsHidden().subscribe(this.selectedOverlayIsHidden);
    this.vm.outputs.shippingSummarySectionIsHidden().subscribe(this.shippingSummarySectionIsHidden);
    this.vm.outputs.shippingSummaryTextViewText().subscribe(this.shippingSummaryTextViewText);
    this.vm.outputs.titleTextViewIsHidden().subscribe(this.titleTextViewIsHidden);
    this.vm.outputs.titleTextViewText().subscribe(this.titleTextViewText);
    this.vm.outputs.usdConversionTextViewText().subscribe(this.usdConversionTextViewText);
    this.vm.outputs.usdConversionTextViewIsHidden().subscribe(this.usdConversionSectionIsHidden);
    this.vm.outputs.whiteOverlayIsHidden().subscribe(this.whiteOverlayIsHidden);
  }

  @Test
  public void testAllGoneTextViewIsHidden() {
    final Project project = ProjectFactory.backedProjectWithRewardLimitReached();
    setUpEnvironment(environment());

    // When an unlimited reward is not backed, hide the 'all gone' header.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.allGoneTextViewIsHidden.assertValues(true);

    // When an unlimited reward is backed, hide the 'all gone' header (distinct until changed).
    final Reward backedReward = project.backing().reward();
    this.vm.inputs.projectAndReward(project, backedReward);
    this.allGoneTextViewIsHidden.assertValues(true);

    // When a backed reward's limit has been reached, hide the 'all gone' header – the selected banner will be shown instead.
    final Reward backedRewardWithLimitReached = backedReward.toBuilder()
      .limit(1)
      .remaining(0)
      .build();
    this.vm.inputs.projectAndReward(project, backedRewardWithLimitReached);
    this.allGoneTextViewIsHidden.assertValues(true);

    // When a reward's limit has been reached and it has not been backed, show the 'all gone' header.
    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.allGoneTextViewIsHidden.assertValues(true, false);
  }

  @Test
  public void testBackersTextViewIsHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    this.backersTextViewIsHidden.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.noReward());
    this.backersTextViewIsHidden.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.backers());
    this.backersTextViewIsHidden.assertValues(true, false);
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
  public void testEstimatedDeliveryDateSectionIsHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Reward with no estimated delivery should not show estimated delivery label.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(null).build());
    this.estimatedDeliveryDateSectionIsHidden.assertValue(true);

    // Reward with estimated delivery should show estimated delivery label.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward().toBuilder().estimatedDeliveryOn(DateTime.now()).build());
    this.estimatedDeliveryDateSectionIsHidden.assertValues(true, false);
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessful() {
    final Project project = ProjectFactory.successfulProject();
    final Reward reward = RewardFactory.reward();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.goToCheckout.assertNoValues();

    this.vm.inputs.rewardClicked();
    this.goToCheckout.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsSuccessfulAndHasBeenBacked() {
    final Project project = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();
    final Reward reward = project.backing().reward();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, reward);
    this.goToCheckout.assertNoValues();

    this.vm.inputs.rewardClicked();
    this.goToCheckout.assertNoValues();
  }

  @Test
  public void testGoToCheckoutWhenProjectIsLive() {
    final Reward reward = RewardFactory.reward();
    final Project liveProject = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(liveProject, reward);
    this.goToCheckout.assertNoValues();

    // When a reward from a live project is clicked, start checkout.
    this.vm.inputs.rewardClicked();
    this.goToCheckout.assertValue(Pair.create(liveProject, reward));
  }

  @Test
  public void testGoToViewPledge() {
    final Project liveProject = ProjectFactory.backedProject();
    final Project successfulProject = ProjectFactory.backedProject().toBuilder()
      .state(Project.STATE_SUCCESSFUL)
      .build();

    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(liveProject, liveProject.backing().reward());
    this.goToViewPledge.assertNoValues();

    // When the project is still live, don't go to 'view pledge'. Should go to checkout instead.
    this.vm.inputs.rewardClicked();
    this.goToViewPledge.assertNoValues();

    // When project is successful but not backed, don't go to view pledge.
    this.vm.inputs.projectAndReward(successfulProject, RewardFactory.reward());
    this.vm.inputs.rewardClicked();
    this.goToViewPledge.assertNoValues();

    // When project is successful and backed, go to view pledge.
    this.vm.inputs.projectAndReward(successfulProject, successfulProject.backing().reward());
    this.goToViewPledge.assertNoValues();
    this.vm.inputs.rewardClicked();
    this.goToViewPledge.assertValues(successfulProject);
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
  public void testLimitAndBackersSeparatorIsHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // When reward has no limit or backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.noBackers());
    this.limitAndBackersSeparatorIsHidden.assertValues(true);

    // When reward has no limit and backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.limitAndBackersSeparatorIsHidden.assertValues(true);

    // When reward has limit and no backers, separator should be hidden.
    this.vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().backersCount(0).build());
    this.limitAndBackersSeparatorIsHidden.assertValues(true);

    // When reward has limit and backers, separator should be visible.
    this.vm.inputs.projectAndReward(project, RewardFactory.limited().toBuilder().build());
    this.limitAndBackersSeparatorIsHidden.assertValues(true, false);
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
    this.limitAndRemainingTextViewIsHidden.assertValue(false);

    // When reward's limit has been reached, don't show quantity.
    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.limitAndRemainingTextViewIsHidden.assertValues(false, true);

    // When reward has no limit, don't show quantity (distinct until changed).
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.limitAndRemainingTextViewIsHidden.assertValues(false, true);
  }

  @Test
  public void testLimitHeaderIsHidden() {
    setUpEnvironment(environment());

    // If the reward is limited and has not been backed, show the limit header.
    final Project backedProjectWithLimitedReward = ProjectFactory.backedProjectWithRewardLimited();
    this.vm.inputs.projectAndReward(backedProjectWithLimitedReward, RewardFactory.limited());
    this.limitHeaderIsHidden.assertValues(false);

    // If the reward is limited and has been backed, don't show the limit header.
    this.vm.inputs.projectAndReward(backedProjectWithLimitedReward, backedProjectWithLimitedReward.backing().reward());
    this.limitHeaderIsHidden.assertValues(false, true);

    // If the reward is not limited, don't show the limit header.
    this.vm.inputs.projectAndReward(ProjectFactory.project(), RewardFactory.reward());
    this.limitHeaderIsHidden.assertValues(false, true);
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
    this.rewardsItemsAreHidden.assertValue(true);
    this.rewardsItemList.assertValue(emptyList());

    final Reward itemizedReward = RewardFactory.itemized();
    this.vm.inputs.projectAndReward(project, itemizedReward);
    this.rewardsItemsAreHidden.assertValues(true, false);
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
    this.titleTextViewIsHidden.assertValues(true);
    this.titleTextViewText.assertNoValues();

    // Reward with title should be visible.
    final String title = "Digital bundle";
    final Reward rewardWithTitle = RewardFactory.reward().toBuilder()
      .title(title)
      .build();
    this.vm.inputs.projectAndReward(project, rewardWithTitle);
    this.titleTextViewIsHidden.assertValues(true, false);
    this.titleTextViewText.assertValue(title);
  }

  @Test
  public void testSelectedHeaderAndOverlay() {
    final Project backedProject = ProjectFactory.backedProject();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(backedProject, backedProject.backing().reward());
    this.selectedHeaderIsHidden.assertValue(false);
    this.selectedOverlayIsHidden.assertValue(false);

    this.vm.inputs.projectAndReward(backedProject, RewardFactory.reward());
    this.selectedHeaderIsHidden.assertValues(false, true);
    this.selectedOverlayIsHidden.assertValues(false, true);
  }

  @Test
  public void testShippingSummary() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    // Reward with no shipping should hide shipping summary section and not emit a shipping summary string.
    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.shippingSummaryTextViewText.assertNoValues();
    this.shippingSummarySectionIsHidden.assertValue(true);

    // Reward with shipping should show shipping summary section and emit a shipping summary string.
    final Reward rewardWithShipping = RewardFactory.rewardWithShipping();
    this.vm.inputs.projectAndReward(project, rewardWithShipping);
    this.shippingSummaryTextViewText.assertValue(rewardWithShipping.shippingSummary());
    this.shippingSummarySectionIsHidden.assertValues(true, false);
  }

  @Test
  public void testUsdConversionForNonUSProject() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    final CurrentConfigType currentConfig = environment.currentConfig();
    environment.currentConfig().config(config);
    setUpEnvironment(environment);

    // Set project's country to CA.
    final Project project = ProjectFactory.caProject();
    final Reward reward = RewardFactory.reward();

    // USD conversion should be shown.
    this.vm.inputs.projectAndReward(project, reward);
    this.usdConversionTextViewText.assertValueCount(1);
    this.usdConversionSectionIsHidden.assertValue(false);

    // Set user's country to CA (any country except the US is fine).
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should now be hidden.
    this.usdConversionTextViewText.assertValueCount(1);
    this.usdConversionSectionIsHidden.assertValues(false, true);
  }

  @Test
  public void testUsdConversionNotShownForUSProject() {
    // Set user's country to US.
    final Config config = ConfigFactory.configForUSUser();
    final Environment environment = environment();
    final CurrentConfigType currentConfig = environment.currentConfig();
    environment.currentConfig().config(config);
    setUpEnvironment(environment);

    // Set project's country to US.
    final Project project = ProjectFactory.project().toBuilder().country("US").build();
    final Reward reward = RewardFactory.reward();

    // USD conversion should not be shown.
    this.vm.inputs.projectAndReward(project, reward);
    this.usdConversionTextViewText.assertNoValues();
    this.usdConversionSectionIsHidden.assertValue(true);

    // Set user's country to CA.
    currentConfig.config(ConfigFactory.configForCAUser());

    // USD conversion should still not be shown (distinct until changed).
    this.usdConversionTextViewText.assertNoValues();
    this.usdConversionSectionIsHidden.assertValues(true);
  }

  @Test
  public void testUsdConversionTextRoundsUp() {
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
    this.usdConversionTextViewText.assertValue("$1");
  }

  @Test
  public void testWhiteOverlayIsHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.whiteOverlayIsHidden.assertValue(true);

    final Project backedProjectWithRewardLimitReached = ProjectFactory.backedProjectWithRewardLimitReached();
    this.vm.inputs.projectAndReward(backedProjectWithRewardLimitReached, backedProjectWithRewardLimitReached.backing().reward());
    this.whiteOverlayIsHidden.assertValues(true);

    this.vm.inputs.projectAndReward(project, RewardFactory.limitReached());
    this.whiteOverlayIsHidden.assertValues(true, false);
  }

  @Test
  public void testNonEmptyRewardsDescriptionAreShown() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.reward());
    this.rewardDescriptionIsHidden.assertValue(false);
  }

  @Test
  public void testEmptyRewardsDescriptionAreHidden() {
    final Project project = ProjectFactory.project();
    setUpEnvironment(environment());

    this.vm.inputs.projectAndReward(project, RewardFactory.noDescription());
    this.rewardDescriptionIsHidden.assertValue(true);
  }
}
