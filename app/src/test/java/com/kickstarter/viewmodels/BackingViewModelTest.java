package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.BackingFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.factories.RewardFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.models.User;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static java.util.Collections.emptyList;

public final class BackingViewModelTest extends KSRobolectricTestCase {
  private BackingViewModel.ViewModel vm;
  private final TestSubscriber<String> backerNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> backerNumberTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> backingStatusTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> backingAmountAndDateTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> creatorNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> estimatedDeliverySectionIsGone = new TestSubscriber<>();
  private final TestSubscriber<String> estimatedDeliverySectionTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Void> goBack = new TestSubscriber<>();
  private final TestSubscriber<String> loadBackerAvatar = new TestSubscriber<>();
  private final TestSubscriber<String> loadProjectPhoto = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> rewardMinimumAndDescriptionTextViewText = new TestSubscriber<>();
  private final TestSubscriber<List<RewardsItem>> rewardsItems = new TestSubscriber<>();
  private final TestSubscriber<Boolean> rewardsItemsAreGone = new TestSubscriber<>();
  private final TestSubscriber<String> shippingAmountTextViewText = new TestSubscriber<>();
  private final TestSubscriber<String> shippingLocationTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> shippingSectionIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, Backing>> startMessagesActivity = new TestSubscriber<>();
  private final TestSubscriber<Pair<Project, RefTag>> startProjectActivity = new TestSubscriber<>();
  private final TestSubscriber<Boolean> viewMessagesButtonIsGone = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new BackingViewModel.ViewModel(environment);
    this.vm.outputs.backerNameTextViewText().subscribe(this.backerNameTextViewText);
    this.vm.outputs.backerNumberTextViewText().subscribe(this.backerNumberTextViewText);
    this.vm.outputs.backingStatusTextViewText().subscribe(this.backingStatusTextViewText);
    this.vm.outputs.backingAmountAndDateTextViewText().subscribe(this.backingAmountAndDateTextViewText);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorNameTextViewText);
    this.vm.outputs.estimatedDeliverySectionIsGone().subscribe(this.estimatedDeliverySectionIsGone);
    this.vm.outputs.estimatedDeliverySectionTextViewText().subscribe(this.estimatedDeliverySectionTextViewText);
    this.vm.outputs.goBack().subscribe(this.goBack);
    this.vm.outputs.loadBackerAvatar().subscribe(this.loadBackerAvatar);
    this.vm.outputs.loadProjectPhoto().subscribe(this.loadProjectPhoto);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.rewardMinimumAndDescriptionTextViewText().subscribe(this.rewardMinimumAndDescriptionTextViewText);
    this.vm.outputs.rewardsItems().subscribe(this.rewardsItems);
    this.vm.outputs.rewardsItemsAreGone().subscribe(this.rewardsItemsAreGone);
    this.vm.outputs.shippingAmountTextViewText().subscribe(this.shippingAmountTextViewText);
    this.vm.outputs.shippingLocationTextViewText().subscribe(this.shippingLocationTextViewText);
    this.vm.outputs.shippingSectionIsGone().subscribe(this.shippingSectionIsGone);
    this.vm.outputs.startMessagesActivity().subscribe(this.startMessagesActivity);
    this.vm.outputs.startProjectActivity().subscribe(this.startProjectActivity);
    this.vm.outputs.viewMessagesButtonIsGone().subscribe(this.viewMessagesButtonIsGone);
  }

  @Test
  public void testBackerNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.backerNameTextViewText.assertValues(backing.backer().name());
    this.koalaTest.assertValues(KoalaEvent.VIEWED_PLEDGE_INFO);
  }

  @Test
  public void testBackerNumberTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.backerNumberTextViewText.assertValues(NumberUtils.format(backing.sequence()));
  }

  @Test
  public void testBackingAmountAndDateTextViewText() {
    final Backing backing = BackingFactory.backing().toBuilder()
      .amount(50.0f)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.backingAmountAndDateTextViewText.assertValues(Pair.create("$50", DateTimeUtils.fullDate(backing.pledgedAt())));
  }

  @Test
  public void testBackingStatus() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.backingStatusTextViewText.assertValue(backing.status());
  }

  @Test
  public void testCreatorNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.creatorNameTextViewText.assertValues(backing.project().creator().name());
  }

  @Test
  public void testEstimatedDeliverySectionIsGone_deliveryNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.estimatedDeliverySectionIsGone.assertValues(true);
  }

  @Test
  public void getTestEstimatedDeliverySectionIsGone_deliveryNotNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(new DateTime().now())
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.estimatedDeliverySectionIsGone.assertValues(false);
  }

  @Test
  public void estimatedDeliverySectionTextViewText() {
    final DateTime testDateTime = new DateTime().now();
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(testDateTime)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.estimatedDeliverySectionTextViewText.assertValues(DateTimeUtils.estimatedDeliveryOn(testDateTime));
  }

  @Test
  public void testGoBackOnProjectClick() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.goBack.assertNoValues();

    this.vm.inputs.projectClicked();
    this.goBack.assertValueCount(1);

    // Project context click doesn't start project activity if not from Messages.
    this.startProjectActivity.assertNoValues();
  }

  @Test
  public void testLoadBackerAvatar() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.loadBackerAvatar.assertValues(backing.backer().avatar().medium());
  }

  @Test
  public void testLoadProjectPhoto() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.loadProjectPhoto.assertValues(backing.project().photo().full());
  }

  @Test
  public void testProjectNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.projectNameTextViewText.assertValues(backing.project().name());
  }

  @Test
  public void testRewardMinimumAndDescriptionTextViewText() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(100.0f)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.rewardMinimumAndDescriptionTextViewText.assertValues(Pair.create("$100", backing.reward().description()));
  }

  @Test
  public void testRewardsItemAreHidden() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .rewardsItems(null)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.rewardsItems.assertValues(emptyList());
    this.rewardsItemsAreGone.assertValues(true);
  }

  @Test
  public void testRewardsItemAreEmitted() {
    final Reward reward = RewardFactory.itemized();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.rewardsItems.assertValues(reward.rewardsItems());
    this.rewardsItemsAreGone.assertValues(false);
  }

  @Test
  public void testShipping_withoutShippingLocation() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.shippingLocationTextViewText.assertNoValues();
    this.shippingAmountTextViewText.assertNoValues();
    this.shippingSectionIsGone.assertValues(true);
  }

  @Test
  public void testShipping_withShippingLocation() {
    final Location location = LocationFactory.sydney();
    final Reward reward = RewardFactory.rewardWithShipping();
    final Backing backing = BackingFactory.backing().toBuilder()
      .location(location)
      .reward(reward)
      .rewardId(reward.id())
      .shippingAmount(5.0f)
      .build();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.shippingLocationTextViewText.assertValues("Sydney, AU");
    this.shippingAmountTextViewText.assertValues("$5");
    this.shippingSectionIsGone.assertValues(false);
  }

  @Test
  public void testStartMessagesActivity() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));
    this.vm.inputs.viewMessagesButtonClicked();

    this.startMessagesActivity.assertValues(Pair.create(backing.project(), backing));
  }

  @Test
  public void testStartProjectActivity() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(BackingFactory.backing()));

    this.vm.intent(
      new Intent().putExtra(IntentKey.PROJECT, backing.project()).putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
    );

    this.vm.inputs.projectClicked();
    this.startProjectActivity.assertValues(Pair.create(backing.project(), RefTag.pledgeInfo()));
    this.goBack.assertNoValues();
  }

  @Test
  public void testViewMessagesButtonIsGone_FromMessages() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(
      new Intent().putExtra(IntentKey.PROJECT, backing.project()).putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
    );

    this.viewMessagesButtonIsGone.assertValues(true);
  }

  @Test
  public void testViewMessagesButtonIsVisible() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(new Intent().putExtra(IntentKey.PROJECT, backing.project()));

    this.viewMessagesButtonIsGone.assertValues(false);
  }

  /**
   * Returns an environment with a backing and the backing's current user.
   */
  private @NonNull Environment envWithBacking(final @NonNull Backing backing) {
    return environment().toBuilder()
      .apiClient(
        new MockApiClient() {
          @Override public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project,
            final @NonNull User user) {
            return Observable.just(backing);
          }
        }
      )
      .currentUser(new MockCurrentUser(backing.backer()))
      .build();
  }
}
