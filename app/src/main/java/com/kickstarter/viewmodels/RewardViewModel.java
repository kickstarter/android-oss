package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Config;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.BackingUtils;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.RewardUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.models.RewardsItem;
import com.kickstarter.ui.viewholders.RewardViewHolder;
import com.kickstarter.viewmodels.inputs.RewardViewModelInputs;
import com.kickstarter.viewmodels.outputs.RewardViewModelOutputs;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;
import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.utils.ObjectUtils.isNull;

public final class RewardViewModel extends ActivityViewModel<RewardViewHolder> implements
  RewardViewModelInputs, RewardViewModelOutputs {

  public RewardViewModel(final @NonNull Environment environment) {
    super(environment);

    final CurrentConfigType currentConfig = environment.currentConfig();
    final KSCurrency ksCurrency = environment.ksCurrency();

    final Observable<String> formattedMinimum = projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first));

    final Observable<Boolean> isSelectable = projectAndReward
      .map(pr -> isSelectable(pr.first, pr.second));

    final Observable<Project> project = projectAndReward
      .map(pr -> pr.first);

    final Observable<Reward> reward = projectAndReward
      .map(pr -> pr.second);

    final Observable<Boolean> rewardIsSelected = projectAndReward
      .map(pr -> BackingUtils.isBacked(pr.first, pr.second));

    final Observable<Boolean> shouldDisplayUsdConversion = currentConfig.observable()
      .map(Config::countryCode)
      .compose(combineLatestPair(project.map(Project::country)))
      .map(configCountryAndProjectCountry ->
        ProjectUtils.isUSUserViewingNonUSProject(configCountryAndProjectCountry.first, configCountryAndProjectCountry.second));

    // Hide 'all gone' header if limit has not been reached, or reward has been backed by user.
    projectAndReward
      .map(pr -> !RewardUtils.isLimitReached(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(allGoneHeaderIsHidden);

    reward
      .map(Reward::isNoReward)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(backersTextViewIsHidden);

    reward
      .filter(Reward::isReward)
      .map(Reward::backersCount)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(backersTextViewText);

    reward
      .map(Reward::description)
      .compose(bindToLifecycle())
      .subscribe(descriptionTextViewText);

    reward
      .map(Reward::estimatedDeliveryOn)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(estimatedDeliveryDateTextViewText);

    reward
      .map(Reward::estimatedDeliveryOn)
      .map(ObjectUtils::isNull)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(estimatedDeliveryDateSectionIsHidden);

    isSelectable
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(isClickable);

    projectAndReward
      .filter(pr -> isSelectable(pr.first, pr.second) && pr.first.isLive())
      .compose(takeWhen(rewardClicked))
      .compose(bindToLifecycle())
      .subscribe(goToCheckout);

    projectAndReward
      .compose(takeWhen(rewardClicked))
      .filter(pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second))
      .map(pr -> pr.first)
      .compose(bindToLifecycle())
      .subscribe(goToViewPledge);

    Observable.just(false)
      .compose(takeWhen(projectAndReward))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitAndRemainingSectionIsCenterAligned);

    reward
      .map(RewardUtils::isLimited)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitAndRemainingSectionIsHidden);

    reward
      .map(r -> Pair.create(r.limit(), r.remaining()))
      .filter(lr -> lr.first != null && lr.second != null)
      .map(rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)))
      .compose(bindToLifecycle())
      .subscribe(limitAndRemainingTextViewText);

    // Implement this properly when rewards can be time-limited.
    Observable.just(true)
      .compose(takeWhen(projectAndReward))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitDividerIsHidden);

    // Hide limit header if reward is not limited, or reward has been backed by user.
    projectAndReward
      .map(pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitHeaderIsHidden);

    formattedMinimum
      .compose(bindToLifecycle())
      .subscribe(minimumButtonText);

    projectAndReward
      .map(pr -> isNull(pr.second.title()) || !isSelectable(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(minimumButtonIsHidden);

    projectAndReward
      .map(pr -> isNull(pr.second.title()) || isSelectable(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(minimumTextViewIsHidden);

    formattedMinimum
      .compose(bindToLifecycle())
      .subscribe(minimumTextViewText);

    formattedMinimum
      .compose(
        takeWhen(
          reward
            .map(Reward::title)
            .filter(ObjectUtils::isNull)
        )
      )
      .compose(bindToLifecycle())
      .subscribe(minimumTitleTextViewText);

    reward
      .map(Reward::rewardsItems)
      .compose(coalesce(new ArrayList<RewardsItem>()))
      .compose(bindToLifecycle())
      .subscribe(rewardsItems);

    reward
      .map(RewardUtils::isItemized)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(rewardsItemsAreHidden);

    reward
      .map(Reward::title)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(rewardTitleTextViewText);

    rewardIsSelected
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(selectedHeaderIsHidden);

    rewardIsSelected
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(selectedOverlayIsHidden);

    reward
      .filter(RewardUtils::isShippable)
      .map(Reward::shippingSummary)
      .compose(bindToLifecycle())
      .subscribe(shippingSummaryTextViewText);

    reward
      .map(RewardUtils::isShippable)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(shippingSummarySectionIsHidden);

    // Not implemented in backend yet
    Observable.just(true)
      .compose(takeWhen(projectAndReward))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(timeLimitSectionIsHidden);

    shouldDisplayUsdConversion
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(usdConversionTextViewIsHidden);

    projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first, true, true))
      .compose(takeWhen(
        shouldDisplayUsdConversion
          .filter(BooleanUtils::isTrue)
      ))
      .compose(bindToLifecycle())
      .subscribe(usdConversionTextViewText);

    reward
      .map(RewardUtils::isLimitReached)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(whiteOverlayIsHidden);
  }

  private static boolean isSelectable(final @NonNull Project project, final @NonNull Reward reward) {
    if (BackingUtils.isBacked(project, reward)) {
      return true;
    }

    if (!project.isLive()) {
      return false;
    }

    return !RewardUtils.isLimitReached(reward);
  }

  private final PublishSubject<Pair<Project, Reward>> projectAndReward = PublishSubject.create();
  private final PublishSubject<Void> rewardClicked = PublishSubject.create();

  private final BehaviorSubject<Boolean> allGoneHeaderIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> backersTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Integer> backersTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> descriptionTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<DateTime> estimatedDeliveryDateTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> estimatedDeliveryDateSectionIsHidden = BehaviorSubject.create();
  private final PublishSubject<Pair<Project, Reward>> goToCheckout = PublishSubject.create();
  private final PublishSubject<Project> goToViewPledge = PublishSubject.create();
  private final BehaviorSubject<Boolean> isClickable = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitAndRemainingSectionIsCenterAligned = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitAndRemainingSectionIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Pair<String, String>> limitAndRemainingTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitDividerIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitHeaderIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> minimumButtonText = BehaviorSubject.create();
  private final BehaviorSubject<String> minimumTitleTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> minimumButtonIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> minimumTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> minimumTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<List<RewardsItem>> rewardsItems = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> rewardsItemsAreHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> rewardTitleTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> selectedHeaderIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> selectedOverlayIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> shippingSummarySectionIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> shippingSummaryTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> timeLimitSectionIsCenterAligned = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> timeLimitSectionIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> timeLimitTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> usdConversionTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> usdConversionTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> whiteOverlayIsHidden = BehaviorSubject.create();

  public final RewardViewModelInputs inputs = this;
  public final RewardViewModelOutputs outputs = this;

  @Override public void projectAndReward(final @NonNull Project project, final @NonNull Reward reward) {
    this.projectAndReward.onNext(Pair.create(project, reward));
  }

  @Override public void rewardClicked() {
    rewardClicked.onNext(null);
  }

  @Override public @NonNull Observable<Boolean> allGoneHeaderIsHidden() {
    return allGoneHeaderIsHidden;
  }

  @Override public @NonNull Observable<Boolean> backersTextViewIsHidden() {
    return backersTextViewIsHidden;
  }

  @Override public @NonNull Observable<Integer> backersTextViewText() {
    return backersTextViewText;
  }

  @Override public @NonNull Observable<Boolean> isClickable() {
    return isClickable;
  }

  @Override public @NonNull Observable<String> descriptionTextViewText() {
    return descriptionTextViewText;
  }

  @Override public @NonNull Observable<DateTime> estimatedDeliveryDateTextViewText() {
    return estimatedDeliveryDateTextViewText;
  }

  @Override public @NonNull Observable<Boolean> estimatedDeliveryDateSectionIsHidden() {
    return estimatedDeliveryDateSectionIsHidden;
  }

  @Override public @NonNull Observable<Pair<Project, Reward>> goToCheckout() {
    return goToCheckout;
  }

  @Override public @NonNull Observable<Project> goToViewPledge() {
    return goToViewPledge;
  }

  @Override public @NonNull Observable<Pair<String, String>> limitAndRemainingTextViewText() {
    return limitAndRemainingTextViewText;
  }

  @Override public @NonNull Observable<Boolean> limitDividerIsHidden() {
    return limitDividerIsHidden;
  }

  @Override public @NonNull Observable<Boolean> limitHeaderIsHidden() {
    return limitHeaderIsHidden;
  }

  @Override public @NonNull Observable<String> minimumButtonText() {
    return minimumButtonText;
  }

  @Override public @NonNull Observable<Boolean> minimumButtonIsHidden() {
    return minimumButtonIsHidden;
  }

  @Override public @NonNull Observable<Boolean> minimumTextViewIsHidden() {
    return minimumTextViewIsHidden;
  }

  @Override public @NonNull Observable<String> minimumTextViewText() {
    return minimumTextViewText;
  }

  @Override public @NonNull Observable<String> minimumTitleTextViewText() {
    return minimumTitleTextViewText;
  }

  @Override public @NonNull Observable<Boolean> limitAndRemainingSectionIsCenterAligned() {
    return limitAndRemainingSectionIsCenterAligned;
  }

  @Override public @NonNull Observable<Boolean> limitAndRemainingSectionIsHidden() {
    return limitAndRemainingSectionIsHidden;
  }

  @Override public @NonNull Observable<List<RewardsItem>> rewardsItems() {
    return rewardsItems;
  }

  @Override public @NonNull Observable<Boolean> rewardsItemsAreHidden() {
    return rewardsItemsAreHidden;
  }

  @Override public @NonNull Observable<String> rewardTitleTextViewText() {
    return rewardTitleTextViewText;
  }

  @Override public @NonNull Observable<Boolean> selectedHeaderIsHidden() {
    return selectedHeaderIsHidden;
  }

  @Override public @NonNull Observable<Boolean> selectedOverlayIsHidden() {
    return selectedOverlayIsHidden;
  }

  @Override public @NonNull Observable<Boolean> shippingSummarySectionIsHidden() {
    return shippingSummarySectionIsHidden;
  }

  @Override public @NonNull Observable<String> shippingSummaryTextViewText() {
    return shippingSummaryTextViewText;
  }

  @Override public @NonNull Observable<String> timeLimitTextViewText() {
    return timeLimitTextViewText;
  }

  @Override public @NonNull Observable<Boolean> timeLimitSectionIsCenterAligned() {
    return timeLimitSectionIsCenterAligned;
  }

  @Override public @NonNull Observable<Boolean> timeLimitSectionIsHidden() {
    return timeLimitSectionIsHidden;
  }

  @Override public @NonNull Observable<Boolean> usdConversionTextViewIsHidden() {
    return usdConversionTextViewIsHidden;
  }

  @Override public @NonNull Observable<String> usdConversionTextViewText() {
    return usdConversionTextViewText;
  }

  @Override public @NonNull Observable<Boolean> whiteOverlayIsHidden() {
    return whiteOverlayIsHidden;
  }
}
