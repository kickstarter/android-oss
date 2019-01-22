package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BackingUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.RewardUtils;
import com.kickstarter.models.Avatar;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.BackingActivity;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.zipPair;

public interface BackingViewModel {

  interface Inputs {
    /** Call when the mark as received switch button is checked. */
    void markAsReceivedSwitchChecked(boolean checked);

    /** Call when the project context section is clicked. */
    void projectClicked();

    /** Call when the view messages button is clicked. */
    void viewMessagesButtonClicked();
  }

  interface Outputs {
    /** Set the backer name TextView's text. */
    Observable<String> backerNameTextViewText();

    /** Set the backer number TextView's text. */
    Observable<String> backerNumberTextViewText();

    /** Set the backing status TextView's text. */
    Observable<String> backingStatusTextViewText();

    /** Set the backing amount and date TextView's text. */
    Observable<Pair<String, String>> backingAmountAndDateTextViewText();

    /** Set the creator name TextView's text. */
    Observable<String> creatorNameTextViewText();

    /** Whether to hide the estimated delivery date section. */
    Observable<Boolean> estimatedDeliverySectionIsGone();

    /** text date for the estimated delivery section. */
    Observable<String> estimatedDeliverySectionTextViewText();

    /** Navigate back. */
    Observable<Void> goBack();

    /** Load the backer avatar given the URL. */
    Observable<String> loadBackerAvatar();

    /** Load the project photo given the URL. */
    Observable<String> loadProjectPhoto();

    /** Emits a boolean that determines if mark as received switch should be checked. */
    Observable<Boolean> markAsReceivedIsChecked();

    /** Set the project name TextView's text. */
    Observable<String> projectNameTextViewText();

    /** Emits a boolean that determines if mark as received section is gone. */
    Observable<Boolean> receivedSectionIsGone();

    /** Set the reward minimum and description TextView's text. */
    Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText();

    /** Show the rewards items. */
    Observable<List<RewardsItem>> rewardsItemList();

    /** Returns `true` if the items section should be gone, `false` otherwise. */
    Observable<Boolean> rewardsItemsAreGone();

    /** Set the shipping amount TextView's text. */
    Observable<String> shippingAmountTextViewText();

    /** Set the shipping location TextView's text. */
    Observable<String> shippingLocationTextViewText();

    /** Set the visibility of the shipping section.*/
    Observable<Boolean> shippingSectionIsGone();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.MessagesActivity}. */
    Observable<Pair<Project, Backing>> startMessagesActivity();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.ProjectActivity}. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** Emits a boolean to determine when the View Messages button should be gone. */
    Observable<Boolean> viewMessagesButtonIsGone();
  }

  final class ViewModel extends ActivityViewModel<BackingActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final KSCurrency ksCurrency;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.ksCurrency = environment.ksCurrency();

      final Observable<User> backerFromIntent = intent()
        .map(i -> i.getParcelableExtra(IntentKey.BACKER))
        .ofType(User.class);

      final Observable<Project> project = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class);

      final Observable<Boolean> isFromMessagesActivity = intent()
        .map(i -> i.getBooleanExtra(IntentKey.IS_FROM_MESSAGES_ACTIVITY, false))
        .ofType(Boolean.class);

      final Observable<Backing> backing = Observable.combineLatest(project, backerFromIntent, Pair::create)
        .switchMap(pb -> this.client.fetchProjectBacking(pb.first, pb.second))
        .compose(neverError())
        .share();

      final Observable<User> backer = backing
        .map(Backing::backer);

      final Observable<Backing> shippableBacking = backing
        .filter(BackingUtils::isShippable);

      final Observable<Reward> reward = backing
        .map(Backing::reward)
        .filter(ObjectUtils::isNotNull);

      final Observable<String> status = backing
        .map(Backing::status);

      Observable.zip(project, backing, Pair::create)
        .compose(takeWhen(this.viewMessagesButtonClicked))
        .compose(bindToLifecycle())
        .subscribe(this.startMessagesActivity);

      backing
        .map(Backing::sequence)
        .map(NumberUtils::format)
        .compose(bindToLifecycle())
        .subscribe(this.backerNumberTextViewText);

      backer
        .map(User::name)
        .compose(bindToLifecycle())
        .subscribe(this.backerNameTextViewText);

      project
        .compose(zipPair(backing))
        .map(pb -> backingAmountAndDate(this.ksCurrency, pb.first, pb.second))
        .compose(bindToLifecycle())
        .subscribe(this.backingAmountAndDateTextViewText);

      status
        .compose(bindToLifecycle())
        .subscribe(this.backingStatusTextViewText);

      project
        .map(p -> p.creator().name())
        .compose(bindToLifecycle())
        .subscribe(this.creatorNameTextViewText);

      Observable.zip(
        isFromMessagesActivity.filter(BooleanUtils::isTrue),
        project,
        Pair::create
      )
        .map(PairUtils::second)
        .compose(takeWhen(this.projectClicked))
        .map(p -> Pair.create(p, RefTag.pledgeInfo()))
        .subscribe(this.startProjectActivity::onNext);

      isFromMessagesActivity
        .filter(BooleanUtils::isFalse)
        .compose(takeWhen(this.projectClicked))
        .compose(ignoreValues())
        .subscribe(this.goBack::onNext);

      backer
        .map(User::avatar)
        .map(Avatar::medium)
        .compose(bindToLifecycle())
        .subscribe(this.loadBackerAvatar);

      project
        .map(Project::photo)
        .filter(ObjectUtils::isNotNull)
        .map(Photo::full)
        .compose(bindToLifecycle())
        .subscribe(this.loadProjectPhoto);

      project
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectNameTextViewText);

      project
        .compose(zipPair(backing.map(Backing::reward)))
        .map(pr -> rewardMinimumAndDescription(this.ksCurrency, pr.first, pr.second))
        .compose(bindToLifecycle())
        .subscribe(this.rewardMinimumAndDescriptionTextViewText);

      reward
        .map(Reward::rewardsItems)
        .compose(Transformers.coalesce(new ArrayList<RewardsItem>()))
        .compose(bindToLifecycle())
        .subscribe(this.rewardsItemList);

      reward
        .map(RewardUtils::isItemized)
        .map(BooleanUtils::negate)
        .compose(bindToLifecycle())
        .subscribe(this.rewardsItemsAreGone);

      reward
        .map(Reward::estimatedDeliveryOn)
        .map(ObjectUtils::isNull)
        .compose(bindToLifecycle())
        .subscribe(this.estimatedDeliverySectionIsGone);

      reward
        .map(Reward::estimatedDeliveryOn)
        .filter(ObjectUtils::isNotNull)
        .map(DateTimeUtils::estimatedDeliveryOn)
        .compose(bindToLifecycle())
        .subscribe(this.estimatedDeliverySectionTextViewText);

      project
        .compose(zipPair(shippableBacking))
        .map(pb -> this.ksCurrency.formatWithProjectCurrency(pb.second.shippingAmount(), pb.first, RoundingMode.UP))
        .compose(bindToLifecycle())
        .subscribe(this.shippingAmountTextViewText);

      backing
        .map(Backing::location)
        .filter(ObjectUtils::isNotNull)
        .map(Location::displayableName)
        .compose(bindToLifecycle())
        .subscribe(this.shippingLocationTextViewText);

      backing
        .map(BackingUtils::isShippable)
        .map(BooleanUtils::negate)
        .compose(bindToLifecycle())
        .subscribe(this.shippingSectionIsGone);

      isFromMessagesActivity
        .map(BooleanUtils::isTrue)
        .compose(bindToLifecycle())
        .subscribe(this.viewMessagesButtonIsGone);

      project
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedPledgeInfo);

      backing
        .map(Backing::backerCompletedAt)
        .map(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(this.markAsReceivedIsChecked);

      final Observable<Pair<Project, Backing>> projectAndBacking = Observable
        .combineLatest(project, backing, Pair::create);

      projectAndBacking
        .compose(takePairWhen(this.markAsReceivedSwitchChecked))
        // combine the project, backing, and checked boolean (<<Project,Backing>, Checked>) to make client call
        .switchMap(pbc -> this.client.postBacking(pbc.first.first, pbc.first.second, pbc.second))
        .compose(bindToLifecycle())
        .share()
        .subscribe();

      final Observable<Boolean> rewardIsReceivable = backing
        .map(Backing::reward)
        .map(r -> ObjectUtils.isNotNull(r) && !RewardUtils.isNoReward(r));

      final Observable<Boolean> backingIsCollected = status
        .map(s -> s.equals(Backing.STATUS_COLLECTED));

      rewardIsReceivable
        .compose(combineLatestPair(backingIsCollected))
        .map(isReceivableAndCollected -> isReceivableAndCollected.first && isReceivableAndCollected.second)
        .map(BooleanUtils::negate)
        .compose(bindToLifecycle())
        .subscribe(this.receivedSectionIsGone);
    }

    private static @NonNull Pair<String, String> backingAmountAndDate(final @NonNull KSCurrency ksCurrency,
      final @NonNull Project project, final @NonNull Backing backing) {

      final String amount = ksCurrency.formatWithProjectCurrency(backing.amount(), project, RoundingMode.UP);
      final String date = DateTimeUtils.fullDate(backing.pledgedAt());

      return Pair.create(amount, date);
    }

    private static @NonNull Pair<String, String> rewardMinimumAndDescription(final @NonNull KSCurrency ksCurrency,
      final @NonNull Project project, final @NonNull Reward reward) {

      final String minimum = ksCurrency.formatWithProjectCurrency(reward.minimum(), project, RoundingMode.UP);
      return Pair.create(minimum, reward.description());
    }

    private final PublishSubject<Void> projectClicked = PublishSubject.create();
    private final PublishSubject<Void> viewMessagesButtonClicked = PublishSubject.create();
    private final PublishSubject<Boolean> markAsReceivedSwitchChecked = PublishSubject.create();

    private final BehaviorSubject<String> backerNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> backerNumberTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> backingStatusTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Pair<String, String>> backingAmountAndDateTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> creatorNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> estimatedDeliverySectionIsGone = BehaviorSubject.create();
    private final BehaviorSubject<String> estimatedDeliverySectionTextViewText = BehaviorSubject.create();
    private final PublishSubject<Void> goBack = PublishSubject.create();
    private final BehaviorSubject<String> loadBackerAvatar = BehaviorSubject.create();
    private final BehaviorSubject<String> loadProjectPhoto = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> markAsReceivedIsChecked = BehaviorSubject.create();
    private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> receivedSectionIsGone = BehaviorSubject.create();
    private final BehaviorSubject<Pair<String, String>> rewardMinimumAndDescriptionTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<List<RewardsItem>> rewardsItemList = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> rewardsItemsAreGone = BehaviorSubject.create();
    private final BehaviorSubject<String> shippingAmountTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<String> shippingLocationTextViewText = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> shippingSectionIsGone = BehaviorSubject.create();
    private final PublishSubject<Pair<Project, Backing>> startMessagesActivity = PublishSubject.create();
    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();
    private final BehaviorSubject<Boolean> viewMessagesButtonIsGone = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void projectClicked() {
      this.projectClicked.onNext(null);
    }
    @Override public void viewMessagesButtonClicked() {
      this.viewMessagesButtonClicked.onNext(null);
    }
    @Override public void markAsReceivedSwitchChecked(final boolean checked) {
      this.markAsReceivedSwitchChecked.onNext(checked);
    }

    @Override public @NonNull Observable<String> backerNameTextViewText() {
      return this.backerNameTextViewText;
    }
    @Override public @NonNull Observable<String> backerNumberTextViewText() {
      return this.backerNumberTextViewText;
    }
    @Override public @NonNull Observable<Pair<String, String>> backingAmountAndDateTextViewText() {
      return this.backingAmountAndDateTextViewText;
    }
    @Override public @NonNull Observable<String> backingStatusTextViewText() {
      return this.backingStatusTextViewText;
    }
    @Override public @NonNull Observable<String> creatorNameTextViewText() {
      return this.creatorNameTextViewText;
    }
    @Override public @NonNull Observable<Boolean> estimatedDeliverySectionIsGone() {
      return this.estimatedDeliverySectionIsGone;
    }
    @Override public @NonNull Observable<String> estimatedDeliverySectionTextViewText() {
      return this.estimatedDeliverySectionTextViewText;
    }
    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<String> loadBackerAvatar() {
      return this.loadBackerAvatar;
    }
    @Override public @NonNull Observable<String> loadProjectPhoto() {
      return this.loadProjectPhoto;
    }
    @Override public @NonNull Observable<Boolean> markAsReceivedIsChecked() {
      return this.markAsReceivedIsChecked;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Boolean> receivedSectionIsGone() {
      return this.receivedSectionIsGone;
    }
    @Override public @NonNull Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText() {
      return this.rewardMinimumAndDescriptionTextViewText;
    }
    @Override public @NonNull Observable<List<RewardsItem>> rewardsItemList() {
      return this.rewardsItemList;
    }
    @Override public @NonNull Observable<Boolean> rewardsItemsAreGone() {
      return this.rewardsItemsAreGone;
    }
    @Override public @NonNull Observable<String> shippingAmountTextViewText() {
      return this.shippingAmountTextViewText;
    }
    @Override public @NonNull Observable<String> shippingLocationTextViewText() {
      return this.shippingLocationTextViewText;
    }
    @Override public @NonNull Observable<Boolean> shippingSectionIsGone() {
      return this.shippingSectionIsGone;
    }
    @Override public @NonNull Observable<Pair<Project, Backing>> startMessagesActivity() {
      return this.startMessagesActivity;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<Boolean> viewMessagesButtonIsGone() {
      return this.viewMessagesButtonIsGone;
    }
  }
}
