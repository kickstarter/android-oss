package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.BackingUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
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
import com.kickstarter.ui.activities.ViewPledgeActivity;
import com.kickstarter.viewmodels.inputs.ViewPledgeViewModelInputs;
import com.kickstarter.viewmodels.outputs.ViewPledgeViewModelOutputs;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;
import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.zipPair;

public final class ViewPledgeViewModel extends ActivityViewModel<ViewPledgeActivity> implements ViewPledgeViewModelInputs,
  ViewPledgeViewModelOutputs  {

  public ViewPledgeViewModel(final @NonNull Environment environment) {
    super(environment);

    final ApiClientType client = environment.apiClient();
    final CurrentUserType currentUser = environment.currentUser();
    final KSCurrency ksCurrency = environment.ksCurrency();

    final Observable<Project> project = intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class);

    final Observable<Backing> backing = project
      .compose(combineLatestPair(currentUser.observable()))
      .filter(pu -> pu.second != null)
      .switchMap(pu -> client.fetchProjectBacking(pu.first, pu.second)
        .retry(3)
        .compose(neverError())
      )
      .share();

    final Observable<User> backer = backing
      .map(Backing::backer);

    final Observable<Backing> shippableBacking = backing
      .filter(BackingUtils::isShippable);

    final Observable<Reward> reward = backing
      .map(Backing::reward)
      .filter(ObjectUtils::isNotNull);

    backing
      .map(Backing::sequence)
      .map(NumberUtils::format)
      .compose(bindToLifecycle())
      .subscribe(backerNumberTextViewText);

    backer
      .map(User::name)
      .compose(bindToLifecycle())
      .subscribe(backerNameTextViewText);

    project
      .compose(zipPair(backing))
      .map(pb -> backingAmountAndDate(ksCurrency, pb.first, pb.second))
      .compose(bindToLifecycle())
      .subscribe(backingAmountAndDateTextViewText);

    backing
      .map(Backing::status)
      .compose(bindToLifecycle())
      .subscribe(backingStatus);

    project
      .map(p -> p.creator().name())
      .compose(bindToLifecycle())
      .subscribe(creatorNameTextViewText);

    goBack = projectClicked;

    backer
      .map(User::avatar)
      .map(Avatar::medium)
      .compose(bindToLifecycle())
      .subscribe(loadBackerAvatar);

    project
      .map(Project::photo)
      .filter(ObjectUtils::isNotNull)
      .map(Photo::full)
      .compose(bindToLifecycle())
      .subscribe(loadProjectPhoto);

    project
      .map(Project::name)
      .compose(bindToLifecycle())
      .subscribe(projectNameTextViewText);

    project
      .compose(zipPair(backing.map(Backing::reward)))
      .map(pr -> rewardMinimumAndDescription(ksCurrency, pr.first, pr.second))
      .compose(bindToLifecycle())
      .subscribe(rewardMinimumAndDescriptionTextViewText);

    reward
      .map(Reward::rewardsItems)
      .compose(coalesce(new ArrayList<RewardsItem>()))
      .compose(bindToLifecycle())
      .subscribe(rewardsItems);

    reward
      .map(RewardUtils::isItemized)
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(rewardsItemsAreHidden);

    project
      .compose(zipPair(shippableBacking))
      .map(pb -> ksCurrency.format(pb.second.shippingAmount(), pb.first))
      .compose(bindToLifecycle())
      .subscribe(shippingAmountTextViewText);

    backing
      .map(Backing::location)
      .filter(ObjectUtils::isNotNull)
      .map(Location::displayableName)
      .compose(bindToLifecycle())
      .subscribe(shippingLocationTextViewText);

    backing
      .map(BackingUtils::isShippable)
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(shippingSectionIsHidden);
  }

  private static Pair<String, String> backingAmountAndDate(final @NonNull KSCurrency ksCurrency,
    final @NonNull Project project, final @NonNull Backing backing) {

    final String amount = ksCurrency.format(backing.amount(), project);
    final String date = DateTimeUtils.fullDate(backing.pledgedAt());

    return Pair.create(amount, date);
  }

  private static Pair<String, String> rewardMinimumAndDescription(final @NonNull KSCurrency ksCurrency,
    final @NonNull Project project, final @NonNull Reward reward) {

    final String minimum = ksCurrency.format(reward.minimum(), project);
    return Pair.create(minimum, reward.reward());
  }

  private final PublishSubject<Void> projectClicked = PublishSubject.create();

  private final BehaviorSubject<String> backerNameTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> backerNumberTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> backingStatus = BehaviorSubject.create();
  private final BehaviorSubject<Pair<String, String>> backingAmountAndDateTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> creatorNameTextViewText = BehaviorSubject.create();
  private final Observable<Void> goBack;
  private final BehaviorSubject<String> loadBackerAvatar = BehaviorSubject.create();
  private final BehaviorSubject<String> loadProjectPhoto = BehaviorSubject.create();
  private final BehaviorSubject<String> projectNameTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Pair<String, String>> rewardMinimumAndDescriptionTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<List<RewardsItem>> rewardsItems = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> rewardsItemsAreHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> shippingAmountTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> shippingLocationTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> shippingSectionIsHidden = BehaviorSubject.create();

  public final ViewPledgeViewModelInputs inputs = this;
  public final ViewPledgeViewModelOutputs outputs = this;

  @Override public void projectClicked() {
    projectClicked.onNext(null);
  }

  @Override public @NonNull Observable<String> backerNameTextViewText() {
    return backerNameTextViewText;
  }

  @Override public @NonNull Observable<String> backerNumberTextViewText() {
    return backerNumberTextViewText;
  }

  @Override public @NonNull Observable<Pair<String, String>> backingAmountAndDateTextViewText() {
    return backingAmountAndDateTextViewText;
  }

  @Override public @NonNull Observable<String> backingStatus() {
    return backingStatus;
  }

  @Override public @NonNull Observable<String> creatorNameTextViewText() {
    return creatorNameTextViewText;
  }

  @Override public @NonNull Observable<Void> goBack() {
    return goBack;
  }

  @Override public @NonNull Observable<String> loadBackerAvatar() {
    return loadBackerAvatar;
  }

  @Override public @NonNull Observable<String> loadProjectPhoto() {
    return loadProjectPhoto;
  }

  @Override public @NonNull Observable<String> projectNameTextViewText() {
    return projectNameTextViewText;
  }

  @Override public @NonNull Observable<Pair<String, String>> rewardMinimumAndDescriptionTextViewText() {
    return rewardMinimumAndDescriptionTextViewText;
  }

  @Override public @NonNull Observable<List<RewardsItem>> rewardsItems() {
    return rewardsItems;
  }

  @Override public @NonNull Observable<Boolean> rewardsItemsAreHidden() {
    return rewardsItemsAreHidden;
  }

  @Override public @NonNull Observable<String> shippingAmountTextViewText() {
    return shippingAmountTextViewText;
  }

  @Override public @NonNull Observable<String> shippingLocationTextViewText() {
    return shippingLocationTextViewText;
  }

  @Override public @NonNull Observable<Boolean> shippingSectionIsHidden() {
    return shippingSectionIsHidden;
  }
}
