package com.kickstarter.viewmodels;

import android.content.Intent;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaEvent;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.mock.factories.BackingFactory;
import com.kickstarter.mock.factories.LocationFactory;
import com.kickstarter.mock.factories.ProjectFactory;
import com.kickstarter.mock.factories.RewardFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.models.User;
import com.kickstarter.ui.IntentKey;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
  private final TestSubscriber<Boolean> markAsReceivedIsChecked = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Boolean> receivedSectionIsGone = new TestSubscriber<>();
  private final TestSubscriber<Pair<String, String>> rewardMinimumAndDescriptionTextViewText = new TestSubscriber<>();
  private final TestSubscriber<List<RewardsItem>> rewardsItemList = new TestSubscriber<>();
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
    this.vm.outputs.markAsReceivedIsChecked().subscribe(this.markAsReceivedIsChecked);
    this.vm.outputs.projectNameTextViewText().subscribe(this.projectNameTextViewText);
    this.vm.outputs.receivedSectionIsGone().subscribe(this.receivedSectionIsGone);
    this.vm.outputs.rewardMinimumAndDescriptionTextViewText().subscribe(this.rewardMinimumAndDescriptionTextViewText);
    this.vm.outputs.rewardsItemList().subscribe(this.rewardsItemList);
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
    final User currentUser = UserFactory.user()
      .toBuilder()
      .name("Kawhi Leonard")
      .build();
    final Backing backing = BackingFactory.backing(currentUser);
    setUpEnvironment(envWithBacking(backing)
      .toBuilder()
      .currentUser(new MockCurrentUser(currentUser))
      .build());

    //User viewing their own pledge
    this.vm.intent(intentForBacking(backing, null));

    this.backerNameTextViewText.assertValues("Kawhi Leonard");
    this.koalaTest.assertValues(KoalaEvent.VIEWED_PLEDGE_INFO);
  }

  @Test
  public void testBackerNameTextViewText_creator() {
    final User backer = UserFactory.user()
      .toBuilder()
      .name("Tim Duncan")
      .build();
    final User creator = UserFactory.creator()
      .toBuilder()
      .name("Kawhi Leonard")
      .build();

    final Backing backing = BackingFactory.backing(backer);

    final Environment environment = envWithBacking(backing)
      .toBuilder()
      .currentUser(new MockCurrentUser(creator))
      .build();
    setUpEnvironment(environment);

    //Creator viewing backer's pledge
    this.vm.intent(intentForBacking(backing, backer));

    this.backerNameTextViewText.assertValues("Tim Duncan");
    this.koalaTest.assertValues(KoalaEvent.VIEWED_PLEDGE_INFO);
  }

  @Test
  public void testBackerNumberTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.backerNumberTextViewText.assertValues(NumberUtils.format(backing.sequence()));
  }

  @Test
  public void testBackingAmountAndDateTextViewText() {
    final Backing backing = BackingFactory.backing().toBuilder()
      .amount(50.0f)
      .build();
    setUpEnvironmentAndIntent(backing);

    this.backingAmountAndDateTextViewText.assertValue(Pair.create("$50", DateTimeUtils.fullDate(backing.pledgedAt())));
  }

  @Test
  public void testBackingStatus() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.backingStatusTextViewText.assertValue(backing.status());
  }

  @Test
  public void testCreatorNameTextViewText() {
    final User creator = UserFactory.creator()
      .toBuilder()
      .name("Megan Rapinoe")
      .build();

    final Project project = ProjectFactory.project()
      .toBuilder()
      .creator(creator)
      .build();

    final Backing backing = BackingFactory.backing()
      .toBuilder()
      .project(project)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.creatorNameTextViewText.assertValues("Megan Rapinoe");
  }

  @Test
  public void testEstimatedDeliverySectionIsGone_deliveryNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.estimatedDeliverySectionIsGone.assertValues(true);
  }

  @Test
  public void testEstimatedDeliverySectionIsGone_deliveryNotNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(DateTime.now())
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.estimatedDeliverySectionIsGone.assertValues(false);
  }

  @Test
  public void estimatedDeliverySectionTextViewText() {
    final DateTime testDateTime = DateTime.now();
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(testDateTime)
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.estimatedDeliverySectionTextViewText.assertValues(DateTimeUtils.estimatedDeliveryOn(testDateTime));
  }

  @Test
  public void testGoBackOnProjectClick() {
    setUpEnvironmentAndIntent(BackingFactory.backing());

    this.goBack.assertNoValues();

    this.vm.inputs.projectClicked();
    this.goBack.assertValueCount(1);

    // Project context click doesn't start project activity if not from Messages.
    this.startProjectActivity.assertNoValues();
  }

  @Test
  public void testLoadBackerAvatar() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.loadBackerAvatar.assertValues(backing.backer().avatar().medium());
  }

  @Test
  public void testLoadProjectPhoto() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.loadProjectPhoto.assertValues(backing.project().photo().full());
  }

  @Test
  public void testMarkAsReceivedIsChecked_isFalse_whenBackingIsNotBackerCompleted() {
    final Backing initialBacking = BackingFactory.backing();

    setUpEnvironmentAndIntent(initialBacking);

    this.markAsReceivedIsChecked.assertValue(false);

    final Backing updatedBacking = initialBacking
      .toBuilder()
      .backerCompletedAt(DateTime.now())
      .build();

    setUpEnvironmentAndIntent(updatedBacking);

    this.vm.inputs.markAsReceivedSwitchChecked(true);
    this.markAsReceivedIsChecked.assertValues(false, true);
  }

  @Test
  public void testMarkAsReceivedIsChecked_isTrue_whenBackingIsBackerCompleted() {
    final Backing initialBacking = BackingFactory.backing()
      .toBuilder()
      .backerCompletedAt(DateTime.now())
      .build();

    setUpEnvironmentAndIntent(initialBacking);

    this.markAsReceivedIsChecked.assertValues(true);

    final Backing updatedBacking = initialBacking
      .toBuilder()
      .backerCompletedAt(null)
      .build();

    setUpEnvironmentAndIntent(updatedBacking);

    this.vm.inputs.markAsReceivedSwitchChecked(true);
    this.markAsReceivedIsChecked.assertValues(true, false);
  }

  @Test
  public void testProjectNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.projectNameTextViewText.assertValues(backing.project().name());
  }

  @Test
  public void testReceivedSectionIsGone_currentUser() {
    setUpEnvironmentAndIntent(BackingFactory.backing(Backing.STATUS_CANCELED));

    this.receivedSectionIsGone.assertValuesAndClear(true);

    setUpEnvironmentAndIntent(BackingFactory.backing(Backing.STATUS_DROPPED));

    this.receivedSectionIsGone.assertValuesAndClear(true);

    setUpEnvironmentAndIntent(BackingFactory.backing(Backing.STATUS_ERRORED));

    this.receivedSectionIsGone.assertValuesAndClear(true);

    setUpEnvironmentAndIntent(BackingFactory.backing(Backing.STATUS_PLEDGED));

    this.receivedSectionIsGone.assertValuesAndClear(true);

    setUpEnvironmentAndIntent(BackingFactory.backing(Backing.STATUS_PREAUTH));

    this.receivedSectionIsGone.assertValuesAndClear(true);

    final Backing backingWithNullReward = BackingFactory.backing(Backing.STATUS_COLLECTED)
      .toBuilder()
      .reward(null)
      .build();

    setUpEnvironmentAndIntent(backingWithNullReward);

    this.receivedSectionIsGone.assertValuesAndClear(true);

    final Backing backingWithNoReward = BackingFactory.backing(Backing.STATUS_COLLECTED)
      .toBuilder()
      .reward(RewardFactory.noReward())
      .build();
    setUpEnvironmentAndIntent(backingWithNoReward);

    this.receivedSectionIsGone.assertValuesAndClear(true);

    final Backing collectedBacking = BackingFactory.backing(Backing.STATUS_COLLECTED);
    setUpEnvironmentAndIntent(collectedBacking);

    this.receivedSectionIsGone.assertValuesAndClear(false);
  }

  @Test
  public void testReceivedSectionIsGone_creator() {
    final Backing collectedBacking = BackingFactory.backing(Backing.STATUS_COLLECTED);
    setUpEnvironment(envWithBacking(collectedBacking));

    //Creator viewing backer's pledge
    this.vm.intent(intentForBacking(collectedBacking, UserFactory.user()));

    this.receivedSectionIsGone.assertValue(true);
  }

  @Test
  public void testRewardMinimumAndDescriptionTextViewText() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(100.0f)
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.rewardMinimumAndDescriptionTextViewText.assertValue(Pair.create("$100", backing.reward().description()));
  }

  @Test
  public void testRewardsItemAreHidden() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .rewardsItems(null)
      .build();

    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.rewardsItemList.assertValue(emptyList());
    this.rewardsItemsAreGone.assertValues(true);
  }

  @Test
  public void testRewardsItemAreEmitted() {
    final Reward reward = RewardFactory.itemized();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();

    setUpEnvironmentAndIntent(backing);

    this.rewardsItemList.assertValue(reward.rewardsItems());
    this.rewardsItemsAreGone.assertValues(false);
  }

  @Test
  public void testShipping_withoutShippingLocation() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

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
    setUpEnvironmentAndIntent(backing);

    this.shippingLocationTextViewText.assertValues("Sydney, AU");
    this.shippingAmountTextViewText.assertValues("$5");
    this.shippingSectionIsGone.assertValues(false);
  }

  @Test
  public void testStartMessagesActivity() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.vm.inputs.viewMessagesButtonClicked();
    this.startMessagesActivity.assertValue(Pair.create(backing.project(), backing));
  }

  @Test
  public void testStartProjectActivity() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(BackingFactory.backing()));

    this.vm.intent(
      new Intent()
        .putExtra(IntentKey.BACKER, backing.backer())
        .putExtra(IntentKey.PROJECT, backing.project())
        .putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
    );

    this.vm.inputs.projectClicked();
    this.startProjectActivity.assertValue(Pair.create(backing.project(), RefTag.pledgeInfo()));
    this.goBack.assertNoValues();
  }

  @Test
  public void testViewMessagesButtonIsGone_FromMessages() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(
      new Intent()
        .putExtra(IntentKey.BACKER, backing.backer())
        .putExtra(IntentKey.PROJECT, backing.project())
        .putExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, true)
    );

    this.viewMessagesButtonIsGone.assertValues(true);
  }

  @Test
  public void testViewMessagesButtonIsVisible() {
    final Backing backing = BackingFactory.backing();
    setUpEnvironmentAndIntent(backing);

    this.viewMessagesButtonIsGone.assertValues(false);
  }

  /**
   * Returns an environment with a backing and logged in user.
   */
  private @NonNull Environment envWithBacking(final @NonNull Backing backing) {
    return environment().toBuilder()
      .apiClient(
        new MockApiClient() {
          @Override
          public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project,
            final @NonNull User user) {
            return Observable.just(backing);
          }
        }
      )
      .currentUser(new MockCurrentUser(UserFactory.user()))
      .build();
  }

  /**
   * Returns an intent with a backing. If the `user` is null, that means the current user is viewing their own backing.
   */
  private @NonNull Intent intentForBacking(final @NonNull Backing backing, final @Nullable User backer) {
    final Intent intent = new Intent().putExtra(IntentKey.PROJECT, backing.project());
    if (backer != null) {
      intent.putExtra(IntentKey.BACKER, backer);
    }
    return intent;
  }

  /**
   * Helper method to set up environment and intent with a backing and logged in user.
   */
  private void setUpEnvironmentAndIntent(final @NonNull Backing backing) {
    setUpEnvironment(envWithBacking(backing));

    this.vm.intent(intentForBacking(backing, null));
  }
}
