package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.RewardUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.ui.viewholders.RewardViewHolder;
import com.kickstarter.viewmodels.inputs.RewardViewModelInputs;
import com.kickstarter.viewmodels.outputs.RewardViewModelOutputs;

import org.joda.time.DateTime;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public final class RewardViewModel extends ActivityViewModel<RewardViewHolder> implements
  RewardViewModelInputs, RewardViewModelOutputs {

  public RewardViewModel(final @NonNull Environment environment) {
    super(environment);

    final CurrentConfigType currentConfig = environment.currentConfig();
    final KSCurrency ksCurrency = environment.ksCurrency();

    final Observable<Project> project = projectAndReward
      .map(pr -> pr.first);

    final Observable<Reward> reward = projectAndReward
      .map(pr -> pr.second);

    final Observable<Boolean> shouldDisplayUsdConversion = currentConfig.observable()
      .map(Config::countryCode)
      .compose(combineLatestPair(project.map(Project::country)))
      .map(configCountryAndProjectCountry ->
        ProjectUtils.isUSUserViewingNonUSProject(configCountryAndProjectCountry.first, configCountryAndProjectCountry.second));

    projectAndReward
      .map(pr -> !pr.first.isBackingRewardId(pr.second.id()))
      .compose(bindToLifecycle())
      .subscribe(selectedRewardIsHidden::onNext);

    reward
      .filter(Reward::isReward)
      .map(Reward::backersCount)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(backers::onNext);

    reward
      .map(Reward::isNoReward)
      .compose(bindToLifecycle())
      .subscribe(backersIsHidden::onNext);

    reward
      .map(Reward::description)
      .compose(bindToLifecycle())
      .subscribe(description::onNext);

    reward
      .map(Reward::estimatedDeliveryOn)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(estimatedDelivery::onNext);

    reward
      .map(Reward::estimatedDeliveryOn)
      .map(ObjectUtils::isNull)
      .compose(bindToLifecycle())
      .subscribe(estimatedDeliveryIsHidden::onNext);

    reward
      .map(Reward::limit)
      .compose(combineLatestPair(reward.map(Reward::remaining)))
      .filter(rr -> rr.first != null && rr.second != null)
      .map(rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)))
      .compose(bindToLifecycle())
      .subscribe(limitAndRemaining::onNext);

    reward
      .map(RewardUtils::isLimitReached)
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(allGoneIsHidden::onNext);

    reward
      .map(RewardUtils::isLimited)
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(limitIsHidden::onNext);

    projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first))
      .compose(bindToLifecycle())
      .subscribe(minimum::onNext);

    projectAndReward
      .map(pr -> isClickable(pr.first, pr.second))
      .compose(bindToLifecycle())
      .subscribe(clickable::onNext);

    reward
      .filter(RewardUtils::isShippable)
      .map(Reward::shippingSummary)
      .compose(bindToLifecycle())
      .subscribe(shippingSummary::onNext);

    reward
      .map(RewardUtils::isShippable)
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(shippingSummaryIsHidden::onNext);

    projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first, true, true))
      .compose(takeWhen(
        shouldDisplayUsdConversion
          .filter(BooleanUtils::isTrue)
      ))
      .compose(bindToLifecycle())
      .subscribe(usdConversion);

    shouldDisplayUsdConversion
      .map(BooleanUtils::negate)
      .compose(bindToLifecycle())
      .subscribe(usdConversionIsHidden::onNext);

    projectAndReward
      .compose(takeWhen(rewardClicked))
      .filter(pr -> isClickable(pr.first, pr.second))
      .compose(bindToLifecycle())
      .subscribe(goToCheckout::onNext);
  }

  private static boolean isClickable(final @NonNull Project project, final @NonNull Reward reward) {
    // Can't select a reward when the project isn't live.
    if (!project.isLive()) {
      return false;
    }

    // Can't select a reward when the limit has been reached and the user didn't back it.
    if (RewardUtils.isLimitReached(reward) && !project.isBacking()) {
      return false;
    }

    return true;
  }

  private final PublishSubject<Pair<Project, Reward>> projectAndReward = PublishSubject.create();
  private final PublishSubject<Void> rewardClicked = PublishSubject.create();

  private final BehaviorSubject<Boolean> allGoneIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Integer> backers = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> backersIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> clickable = BehaviorSubject.create();
  private final BehaviorSubject<String> description = BehaviorSubject.create();
  private final BehaviorSubject<DateTime> estimatedDelivery = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> estimatedDeliveryIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Pair<Project, Reward>> goToCheckout = BehaviorSubject.create();
  private final BehaviorSubject<Pair<String, String>> limitAndRemaining = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> minimum = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> selectedRewardIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> shippingSummary = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> shippingSummaryIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> usdConversion = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> usdConversionIsHidden = BehaviorSubject.create();

  public final RewardViewModelInputs inputs = this;
  public final RewardViewModelOutputs outputs = this;

  @Override
  public void projectAndReward(final @NonNull Project project, final @NonNull Reward reward) {
    this.projectAndReward.onNext(Pair.create(project, reward));
  }

  @Override
  public void rewardClicked() {
    rewardClicked.onNext(null);
  }

  @Override
  public @NonNull Observable<Boolean> allGoneIsHidden() {
    return allGoneIsHidden;
  }

  @Override
  public @NonNull Observable<Integer> backers() {
    return backers;
  }

  @Override
  public @NonNull Observable<Boolean> backersIsHidden() {
    return backersIsHidden;
  }

  @Override
  public @NonNull Observable<Boolean> clickable() {
    return clickable;
  }

  @Override
  public @NonNull Observable<String> description() {
    return description;
  }

  @Override
  public @NonNull Observable<DateTime> estimatedDelivery() {
    return estimatedDelivery;
  }

  @Override
  public @NonNull Observable<Boolean> estimatedDeliveryIsHidden() {
    return estimatedDeliveryIsHidden;
  }

  @Override
  public @NonNull Observable<Pair<Project, Reward>> goToCheckout() {
    return goToCheckout;
  }

  @Override
  public @NonNull Observable<Pair<String, String>> limitAndRemaining() {
    return limitAndRemaining;
  }

  @Override
  public @NonNull Observable<Boolean> limitIsHidden() {
    return limitIsHidden;
  }

  @Override
  public @NonNull Observable<String> minimum() {
    return minimum;
  }

  @Override
  public @NonNull Observable<Boolean> selectedRewardIsHidden() {
    return selectedRewardIsHidden;
  }

  @Override
  public @NonNull Observable<String> shippingSummary() {
    return shippingSummary;
  }

  @Override
  public @NonNull Observable<Boolean> shippingSummaryIsHidden() {
    return shippingSummaryIsHidden;
  }

  @Override
  public @NonNull Observable<String> usdConversion() {
    return usdConversion;
  }

  @Override
  public @NonNull Observable<Boolean> usdConversionIsHidden() {
    return usdConversionIsHidden;
  }
}
