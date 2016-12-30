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
import com.kickstarter.libs.utils.IntegerUtils;
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

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;
import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

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
      .subscribe(allGoneTextViewIsHidden);

    reward
      .map(r -> RewardUtils.isNoReward(r) || !RewardUtils.hasBackers(r))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(backersTextViewIsHidden);

    reward
      .filter(r -> RewardUtils.isReward(r) || RewardUtils.hasBackers(r))
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

    reward
      .map(r -> IntegerUtils.isNonZero(r.limit()) && IntegerUtils.isNonZero(r.backersCount()))
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitAndBackersSeparatorIsHidden);

    reward
      .map(RewardUtils::isLimited)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitAndRemainingTextViewIsHidden);

    reward
      .map(r -> Pair.create(r.limit(), r.remaining()))
      .filter(lr -> lr.first != null && lr.second != null)
      .map(rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)))
      .compose(bindToLifecycle())
      .subscribe(limitAndRemainingTextViewText);

    // Hide limit header if reward is not limited, or reward has been backed by user.
    projectAndReward
      .map(pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(limitHeaderIsHidden);

    formattedMinimum
      .compose(bindToLifecycle())
      .subscribe(minimumTextViewText);

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

    reward
      .map(Reward::title)
      .map(ObjectUtils::isNull)
      .compose(bindToLifecycle())
      .subscribe(titleTextViewIsHidden);

    reward
      .map(Reward::title)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(titleTextViewText);

    shouldDisplayUsdConversion
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(usdConversionTextViewIsHidden);

    projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first, true, true, RoundingMode.UP))
      .compose(takeWhen(
        shouldDisplayUsdConversion
          .filter(BooleanUtils::isTrue)
      ))
      .compose(bindToLifecycle())
      .subscribe(usdConversionTextViewText);

    projectAndReward
      .map(pr -> RewardUtils.isLimitReached(pr.second) && !BackingUtils.isBacked(pr.first, pr.second))
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

  private final BehaviorSubject<Boolean> allGoneTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> backersTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Integer> backersTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<String> descriptionTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<DateTime> estimatedDeliveryDateTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> estimatedDeliveryDateSectionIsHidden = BehaviorSubject.create();
  private final PublishSubject<Pair<Project, Reward>> goToCheckout = PublishSubject.create();
  private final PublishSubject<Project> goToViewPledge = PublishSubject.create();
  private final BehaviorSubject<Boolean> isClickable = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitAndBackersSeparatorIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitAndRemainingTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Pair<String, String>> limitAndRemainingTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> limitHeaderIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> minimumTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<List<RewardsItem>> rewardsItems = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> rewardsItemsAreHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> titleTextViewIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> titleTextViewText = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> selectedHeaderIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> selectedOverlayIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> shippingSummarySectionIsHidden = BehaviorSubject.create();
  private final BehaviorSubject<String> shippingSummaryTextViewText = BehaviorSubject.create();
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

  @Override public @NonNull Observable<Boolean> allGoneTextViewIsHidden() {
    return allGoneTextViewIsHidden;
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

  @Override public @NonNull Observable<Boolean> limitAndBackersSeparatorIsHidden() {
    return limitAndBackersSeparatorIsHidden;
  }

  @Override public @NonNull Observable<Boolean> limitAndRemainingTextViewIsHidden() {
    return limitAndRemainingTextViewIsHidden;
  }

  @Override public @NonNull Observable<Pair<String, String>> limitAndRemainingTextViewText() {
    return limitAndRemainingTextViewText;
  }

  @Override public @NonNull Observable<Boolean> limitHeaderIsHidden() {
    return limitHeaderIsHidden;
  }

  @Override public @NonNull Observable<String> minimumTextViewText() {
    return minimumTextViewText;
  }

  @Override public @NonNull Observable<List<RewardsItem>> rewardsItems() {
    return rewardsItems;
  }

  @Override public @NonNull Observable<Boolean> rewardsItemsAreHidden() {
    return rewardsItemsAreHidden;
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

  @Override public @NonNull Observable<Boolean> titleTextViewIsHidden() {
    return titleTextViewIsHidden;
  }

  @Override public @NonNull Observable<String> titleTextViewText() {
    return titleTextViewText;
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
