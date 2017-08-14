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

    final Observable<String> formattedMinimum = this.projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first));

    final Observable<Boolean> isSelectable = this.projectAndReward
      .map(pr -> isSelectable(pr.first, pr.second));

    final Observable<Project> project = this.projectAndReward
      .map(pr -> pr.first);

    final Observable<Reward> reward = this.projectAndReward
      .map(pr -> pr.second);

    final Observable<Boolean> rewardIsSelected = this.projectAndReward
      .map(pr -> BackingUtils.isBacked(pr.first, pr.second));

    final Observable<Boolean> shouldDisplayUsdConversion = currentConfig.observable()
      .map(Config::countryCode)
      .compose(combineLatestPair(project.map(Project::country)))
      .map(configCountryAndProjectCountry ->
        ProjectUtils.isUSUserViewingNonUSProject(configCountryAndProjectCountry.first, configCountryAndProjectCountry.second));

    // Hide 'all gone' header if limit has not been reached, or reward has been backed by user.
    this.projectAndReward
      .map(pr -> !RewardUtils.isLimitReached(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.allGoneTextViewIsHidden);

    reward
      .map(r -> RewardUtils.isNoReward(r) || !RewardUtils.hasBackers(r))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.backersTextViewIsHidden);

    reward
      .filter(r -> RewardUtils.isReward(r) || RewardUtils.hasBackers(r))
      .map(Reward::backersCount)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(this.backersTextViewText);

    reward
      .map(Reward::description)
      .compose(bindToLifecycle())
      .subscribe(this.descriptionTextViewText);

    reward
      .map(Reward::estimatedDeliveryOn)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(this.estimatedDeliveryDateTextViewText);

    reward
      .map(Reward::estimatedDeliveryOn)
      .map(ObjectUtils::isNull)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.estimatedDeliveryDateSectionIsHidden);

    isSelectable
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.isClickable);

    this.projectAndReward
      .filter(pr -> isSelectable(pr.first, pr.second) && pr.first.isLive())
      .compose(takeWhen(this.rewardClicked))
      .compose(bindToLifecycle())
      .subscribe(this.goToCheckout);

    this.projectAndReward
      .compose(takeWhen(this.rewardClicked))
      .filter(pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second))
      .map(pr -> pr.first)
      .compose(bindToLifecycle())
      .subscribe(this.goToViewPledge);

    reward
      .map(r -> IntegerUtils.isNonZero(r.limit()) && IntegerUtils.isNonZero(r.backersCount()))
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.limitAndBackersSeparatorIsHidden);

    reward
      .map(RewardUtils::isLimited)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.limitAndRemainingTextViewIsHidden);

    reward
      .map(r -> Pair.create(r.limit(), r.remaining()))
      .filter(lr -> lr.first != null && lr.second != null)
      .map(rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)))
      .compose(bindToLifecycle())
      .subscribe(this.limitAndRemainingTextViewText);

    // Hide limit header if reward is not limited, or reward has been backed by user.
    this.projectAndReward
      .map(pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.limitHeaderIsHidden);

    formattedMinimum
      .compose(bindToLifecycle())
      .subscribe(this.minimumTextViewText);

    reward
      .map(Reward::rewardsItems)
      .compose(coalesce(new ArrayList<RewardsItem>()))
      .compose(bindToLifecycle())
      .subscribe(this.rewardsItemList);

    reward
      .map(RewardUtils::isItemized)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.rewardsItemsAreHidden);

    rewardIsSelected
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.selectedHeaderIsHidden);

    rewardIsSelected
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.selectedOverlayIsHidden);

    reward
      .filter(RewardUtils::isShippable)
      .map(Reward::shippingSummary)
      .compose(bindToLifecycle())
      .subscribe(this.shippingSummaryTextViewText);

    reward
      .map(RewardUtils::isShippable)
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.shippingSummarySectionIsHidden);

    reward
      .map(Reward::title)
      .map(ObjectUtils::isNull)
      .compose(bindToLifecycle())
      .subscribe(this.titleTextViewIsHidden);

    reward
      .map(Reward::description)
      .map(String::isEmpty)
      .compose(bindToLifecycle())
      .subscribe(this.rewardDescriptionIsHidden);

    reward
      .map(Reward::title)
      .filter(ObjectUtils::isNotNull)
      .compose(bindToLifecycle())
      .subscribe(this.titleTextViewText);

    shouldDisplayUsdConversion
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.usdConversionTextViewIsHidden);

    this.projectAndReward
      .map(pr -> ksCurrency.format(pr.second.minimum(), pr.first, true, true, RoundingMode.UP))
      .compose(takeWhen(
        shouldDisplayUsdConversion
          .filter(BooleanUtils::isTrue)
      ))
      .compose(bindToLifecycle())
      .subscribe(this.usdConversionTextViewText);

    this.projectAndReward
      .map(pr -> RewardUtils.isLimitReached(pr.second) && !BackingUtils.isBacked(pr.first, pr.second))
      .map(BooleanUtils::negate)
      .distinctUntilChanged()
      .compose(bindToLifecycle())
      .subscribe(this.whiteOverlayIsHidden);
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
  private final BehaviorSubject<List<RewardsItem>> rewardsItemList = BehaviorSubject.create();
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
  private final BehaviorSubject<Boolean> rewardDescriptionIsHidden = BehaviorSubject.create();

  public final RewardViewModelInputs inputs = this;
  public final RewardViewModelOutputs outputs = this;

  @Override public void projectAndReward(final @NonNull Project project, final @NonNull Reward reward) {
    this.projectAndReward.onNext(Pair.create(project, reward));
  }

  @Override public void rewardClicked() {
    this.rewardClicked.onNext(null);
  }

  @Override public @NonNull Observable<Boolean> allGoneTextViewIsHidden() {
    return this.allGoneTextViewIsHidden;
  }

  @Override public @NonNull Observable<Boolean> backersTextViewIsHidden() {
    return this.backersTextViewIsHidden;
  }

  @Override public @NonNull Observable<Integer> backersTextViewText() {
    return this.backersTextViewText;
  }

  @Override public @NonNull Observable<Boolean> isClickable() {
    return this.isClickable;
  }

  @Override public @NonNull Observable<String> descriptionTextViewText() {
    return this.descriptionTextViewText;
  }

  @Override public @NonNull Observable<DateTime> estimatedDeliveryDateTextViewText() {
    return this.estimatedDeliveryDateTextViewText;
  }

  @Override public @NonNull Observable<Boolean> estimatedDeliveryDateSectionIsHidden() {
    return this.estimatedDeliveryDateSectionIsHidden;
  }

  @Override public @NonNull Observable<Pair<Project, Reward>> goToCheckout() {
    return this.goToCheckout;
  }

  @Override public @NonNull Observable<Project> goToViewPledge() {
    return this.goToViewPledge;
  }

  @Override public @NonNull Observable<Boolean> limitAndBackersSeparatorIsHidden() {
    return this.limitAndBackersSeparatorIsHidden;
  }

  @Override public @NonNull Observable<Boolean> limitAndRemainingTextViewIsHidden() {
    return this.limitAndRemainingTextViewIsHidden;
  }

  @Override public @NonNull Observable<Pair<String, String>> limitAndRemainingTextViewText() {
    return this.limitAndRemainingTextViewText;
  }

  @Override public @NonNull Observable<Boolean> limitHeaderIsHidden() {
    return this.limitHeaderIsHidden;
  }

  @Override public @NonNull Observable<String> minimumTextViewText() {
    return this.minimumTextViewText;
  }

  @Override public @NonNull Observable<List<RewardsItem>> rewardsItemList() {
    return this.rewardsItemList;
  }

  @Override public @NonNull Observable<Boolean> rewardsItemsAreHidden() {
    return this.rewardsItemsAreHidden;
  }

  @Override public @NonNull Observable<Boolean> selectedHeaderIsHidden() {
    return this.selectedHeaderIsHidden;
  }

  @Override public @NonNull Observable<Boolean> selectedOverlayIsHidden() {
    return this.selectedOverlayIsHidden;
  }

  @Override public @NonNull Observable<Boolean> shippingSummarySectionIsHidden() {
    return this.shippingSummarySectionIsHidden;
  }

  @Override public @NonNull Observable<String> shippingSummaryTextViewText() {
    return this.shippingSummaryTextViewText;
  }

  @Override public @NonNull Observable<Boolean> titleTextViewIsHidden() {
    return this.titleTextViewIsHidden;
  }

  @Override public @NonNull Observable<String> titleTextViewText() {
    return this.titleTextViewText;
  }

  @Override public @NonNull Observable<Boolean> usdConversionTextViewIsHidden() {
    return this.usdConversionTextViewIsHidden;
  }

  @Override public @NonNull Observable<String> usdConversionTextViewText() {
    return this.usdConversionTextViewText;
  }

  @Override public @NonNull Observable<Boolean> whiteOverlayIsHidden() {
    return this.whiteOverlayIsHidden;
  }

  @Override public @NonNull Observable<Boolean> rewardDescriptionIsHidden() {
    return this.rewardDescriptionIsHidden;
  }
}
