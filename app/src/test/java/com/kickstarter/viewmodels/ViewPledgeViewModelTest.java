package com.kickstarter.viewmodels;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.BackingFactory;
import com.kickstarter.factories.LocationFactory;
import com.kickstarter.factories.RewardFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Location;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;
import com.kickstarter.ui.IntentKey;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static java.util.Collections.emptyList;

public final class ViewPledgeViewModelTest extends KSRobolectricTestCase {

  @Test
  public void testBackerNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> backerNameTextViewTextTest = TestSubscriber.create();
    vm.outputs.backerNameTextViewText().subscribe(backerNameTextViewTextTest);

    vm.intent(intent(backing));

    backerNameTextViewTextTest.assertValues(backing.backer().name());
  }

  @Test
  public void testBackerNumberTextViewText() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> backerNumberTextViewTextTest = TestSubscriber.create();
    vm.outputs.backerNumberTextViewText().subscribe(backerNumberTextViewTextTest);

    vm.intent(intent(backing));

    backerNumberTextViewTextTest.assertValues(NumberUtils.format(backing.sequence()));
  }

  @Test
  public void testBackingAmountAndDateTextViewText() {
    final Backing backing = BackingFactory.backing().toBuilder()
      .amount(50.0f)
      .build();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<Pair<String, String>> backingAmountAndDateTextViewTextTest = TestSubscriber.create();
    vm.outputs.backingAmountAndDateTextViewText().subscribe(backingAmountAndDateTextViewTextTest);

    vm.intent(intent(backing));

    backingAmountAndDateTextViewTextTest.assertValues(Pair.create("$50", DateTimeUtils.fullDate(backing.pledgedAt())));
  }

  @Test
  public void testBackingStatus() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> backingStatusTextViewTest = TestSubscriber.create();
    vm.outputs.backingStatus().subscribe(backingStatusTextViewTest);

    vm.intent(intent(backing));

    backingStatusTextViewTest.assertValue(backing.status());
  }

  @Test
  public void testCreatorNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> creatorNameTextViewTextTest = TestSubscriber.create();
    vm.outputs.creatorNameTextViewText().subscribe(creatorNameTextViewTextTest);

    vm.intent(intent(backing));

    creatorNameTextViewTextTest.assertValues(backing.project().creator().name());
  }

  @Test
  public void testEstimatedDeliverySectionIsGone_deliveryNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(null)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    final ViewPledgeViewModel vm = vm(backing);
    final TestSubscriber<Boolean> estimatedDeliverySectionIsGone = TestSubscriber.create();
    vm.outputs.estimatedDeliverySectionIsGone().subscribe(estimatedDeliverySectionIsGone);

    estimatedDeliverySectionIsGone.assertValues(true);
  }

  @Test
  public void getTestEstimatedDeliverySectionIsGone_deliveryNotNull() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .estimatedDeliveryOn(new DateTime().now())
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    final ViewPledgeViewModel vm = vm(backing);

    vm.intent(intent(backing));
    final TestSubscriber<Boolean> estimatedDeliverySectionIsGone = TestSubscriber.create();
    vm.outputs.estimatedDeliverySectionIsGone().subscribe(estimatedDeliverySectionIsGone);

    estimatedDeliverySectionIsGone.assertValues(false);
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
    final ViewPledgeViewModel vm = vm(backing);
    vm.intent(intent(backing));
    final TestSubscriber<String> estimatedDeliverySectionTextViewText = TestSubscriber.create();
    vm.outputs.estimatedDeliverySectionTextViewText().subscribe(estimatedDeliverySectionTextViewText);

    estimatedDeliverySectionTextViewText.assertValues(DateTimeUtils.estimatedDeliveryOn(testDateTime));
  }

  @Test
  public void testGoBackOnProjectClick() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<Void> goBackTest = TestSubscriber.create();
    vm.outputs.goBack().subscribe(goBackTest);

    vm.intent(intent(backing));
    goBackTest.assertNoValues();

    vm.inputs.projectClicked();
    goBackTest.assertValueCount(1);
  }

  @Test
  public void testLoadBackerAvatar() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> loadBackerAvatarTest = TestSubscriber.create();
    vm.outputs.loadBackerAvatar().subscribe(loadBackerAvatarTest);

    vm.intent(intent(backing));

    loadBackerAvatarTest.assertValues(backing.backer().avatar().medium());
  }

  @Test
  public void testLoadProjectPhoto() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> loadProjectPhotoTest = TestSubscriber.create();
    vm.outputs.loadProjectPhoto().subscribe(loadProjectPhotoTest);

    vm.intent(intent(backing));

    loadProjectPhotoTest.assertValues(backing.project().photo().full());
  }

  @Test
  public void testProjectNameTextViewText() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> projectNameTextViewTextTest = TestSubscriber.create();
    vm.outputs.projectNameTextViewText().subscribe(projectNameTextViewTextTest);

    vm.intent(intent(backing));

    projectNameTextViewTextTest.assertValues(backing.project().name());
  }

  @Test
  public void testRewardMinimumAndDescriptionTextViewText() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .minimum(100.0f)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<Pair<String, String>> rewardMinimumAndDescriptionTextViewTextTest = TestSubscriber.create();
    vm.outputs.rewardMinimumAndDescriptionTextViewText().subscribe(rewardMinimumAndDescriptionTextViewTextTest);

    vm.intent(intent(backing));

    rewardMinimumAndDescriptionTextViewTextTest.assertValues(Pair.create("$100", backing.reward().description()));
  }

  @Test
  public void testRewardsItemAreHidden() {
    final Reward reward = RewardFactory.reward().toBuilder()
      .rewardsItems(null)
      .build();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<List<RewardsItem>> rewardsItemsTest = TestSubscriber.create();
    vm.outputs.rewardsItems().subscribe(rewardsItemsTest);
    final TestSubscriber<Boolean> rewardsItemsAreHiddenTest = TestSubscriber.create();
    vm.outputs.rewardsItemsAreHidden().subscribe(rewardsItemsAreHiddenTest);

    vm.intent(intent(backing));

    rewardsItemsTest.assertValues(emptyList());
    rewardsItemsAreHiddenTest.assertValues(true);
  }

  @Test
  public void testRewardsItemAreEmitted() {
    final Reward reward = RewardFactory.itemized();
    final Backing backing = BackingFactory.backing().toBuilder()
      .reward(reward)
      .build();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<List<RewardsItem>> rewardsItemsTest = TestSubscriber.create();
    vm.outputs.rewardsItems().subscribe(rewardsItemsTest);
    final TestSubscriber<Boolean> rewardsItemsAreHiddenTest = TestSubscriber.create();
    vm.outputs.rewardsItemsAreHidden().subscribe(rewardsItemsAreHiddenTest);

    vm.intent(intent(backing));

    rewardsItemsTest.assertValues(reward.rewardsItems());
    rewardsItemsAreHiddenTest.assertValues(false);
  }

  @Test
  public void testShipping_withoutShippingLocation() {
    final Backing backing = BackingFactory.backing();
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> shippingLocationTextViewTextTest = TestSubscriber.create();
    vm.outputs.shippingLocationTextViewText().subscribe(shippingLocationTextViewTextTest);
    final TestSubscriber<String> shippingAmountTextViewTextTest = TestSubscriber.create();
    vm.outputs.shippingAmountTextViewText().subscribe(shippingAmountTextViewTextTest);
    final TestSubscriber<Boolean> shippingSectionIsHiddenTest = TestSubscriber.create();
    vm.outputs.shippingSectionIsHidden().subscribe(shippingSectionIsHiddenTest);

    vm.intent(intent(backing));

    shippingLocationTextViewTextTest.assertNoValues();
    shippingAmountTextViewTextTest.assertNoValues();
    shippingSectionIsHiddenTest.assertValues(true);
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
    final ViewPledgeViewModel vm = vm(backing);

    final TestSubscriber<String> shippingLocationTextViewTextTest = TestSubscriber.create();
    vm.outputs.shippingLocationTextViewText().subscribe(shippingLocationTextViewTextTest);
    final TestSubscriber<String> shippingAmountTextViewTextTest = TestSubscriber.create();
    vm.outputs.shippingAmountTextViewText().subscribe(shippingAmountTextViewTextTest);
    final TestSubscriber<Boolean> shippingSectionIsHiddenTest = TestSubscriber.create();
    vm.outputs.shippingSectionIsHidden().subscribe(shippingSectionIsHiddenTest);

    vm.intent(intent(backing));

    shippingLocationTextViewTextTest.assertValues("Sydney, AU");
    shippingAmountTextViewTextTest.assertValues("$5");
    shippingSectionIsHiddenTest.assertValues(false);
  }

  private @NonNull ApiClientType apiClient(final @NonNull Backing backing) {
    return new MockApiClient() {
      @Override
      public @NonNull Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
        return Observable.just(backing);
      }
    };
  }

  private @NonNull Environment environment(final @NonNull Backing backing) {
    return environment().toBuilder()
      .apiClient(apiClient(backing))
      .currentUser(new MockCurrentUser(backing.backer()))
      .build();
  }

  private @NonNull Intent intent(final @NonNull Backing backing) {
    return new Intent().putExtra(IntentKey.PROJECT, backing.project());
  }

  private @NonNull ViewPledgeViewModel vm(final @NonNull Backing backing) {
    return new ViewPledgeViewModel(environment(backing));
  }
}
