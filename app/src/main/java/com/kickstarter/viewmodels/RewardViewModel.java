package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
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

import org.joda.time.DateTime;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.coalesce;
import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface RewardViewModel {

  interface Inputs {
    /** Call with a reward and project when data is bound to the view. */
    void projectAndReward(Project project, Reward reward);

    /** Call when the user clicks on a reward.*/
    void rewardClicked();
  }

  interface Outputs {
    /** Returns `true` if the all gone TextView should be gone, `false` otherwise. */
    Observable<Boolean> allGoneTextViewIsGone();

    /** Set the backers TextView's text. */
    Observable<Integer> backersTextViewText();

    /** Returns `true` if the number of backers TextView should be hidden, `false` otherwise. */
    Observable<Boolean> backersTextViewIsGone();

    /** Returns `true` if the USD conversion section should be hidden, `false` otherwise. */
    Observable<Boolean> conversionTextViewIsGone();

    /** Set the USD conversion. */
    Observable<String> conversionTextViewText();

    /** Set the description TextView's text. */
    Observable<String> descriptionTextViewText();

    /** Set the estimated delivery date TextView's text. */
    Observable<DateTime> estimatedDeliveryDateTextViewText();

    /** Returns `true` if the estimated delivery section should be hidden, `false` otherwise. */
    Observable<Boolean> estimatedDeliveryDateSectionIsGone();

    /** Returns `true` if reward can be clicked, `false` otherwise. */
    Observable<Boolean> isClickable();

    /** Returns `true` if the separator between the limit and backers TextViews should be hidden, `false` otherwise. */
    Observable<Boolean> limitAndBackersSeparatorIsGone();

    /** Returns `true` if the limit TextView should be hidden, `false` otherwise. */
    Observable<Boolean> limitAndRemainingTextViewIsGone();

    /** Set the limit and remaining TextView's text. */
    Observable<Pair<String, String>> limitAndRemainingTextViewText();

    /** Returns `true` if the limit header should be hidden, `false` otherwise. */
    Observable<Boolean> limitHeaderIsGone();

    /** Set the minimum TextView's text. */
    Observable<String> minimumTextViewText();

    /** Returns `true` if the reward description is empty and should be hidden in the UI. */
    Observable<Boolean> rewardDescriptionIsGone();

    /** Show the rewards items. */
    Observable<List<RewardsItem>> rewardsItemList();

    /** Returns `true` if the items section should be hidden, `false` otherwise. */
    Observable<Boolean> rewardsItemsAreGone();

    /** Returns `true` if selected header should be hidden, `false` otherwise. */
    Observable<Boolean> selectedHeaderIsGone();

    /** Returns `true` if the shipping section should be hidden, `false` otherwise. */
    Observable<Boolean> shippingSummarySectionIsGone();

    /** Set the shipping summary TextView's text. */
    Observable<String> shippingSummaryTextViewText();

    /** Start the {@link com.kickstarter.ui.activities.BackingActivity} with the project. */
    Observable<Project> startBackingActivity();

    /** Start {@link com.kickstarter.ui.activities.CheckoutActivity} with the project's reward selected. */
    Observable<Pair<Project, Reward>> startCheckoutActivity();

    /** Returns `true` if the title TextView should be hidden, `false` otherwise. */
    Observable<Boolean> titleTextViewIsGone();

    /** Use the reward's title to set the title text. */
    Observable<String> titleTextViewText();

    /** Returns `true` if the white overlay indicating a reward is disabled should be invisible, `false` otherwise. */
    Observable<Boolean> whiteOverlayIsInvisible();
  }

  final class ViewModel extends ActivityViewModel<RewardViewHolder> implements Inputs, Outputs {
    private final KSCurrency ksCurrency;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.ksCurrency = environment.ksCurrency();

      final Observable<String> formattedMinimum = this.projectAndReward
        .map(pr -> this.ksCurrency.formatWithProjectCurrency(pr.second.minimum(), pr.first, RoundingMode.UP));

      final Observable<Boolean> isSelectable = this.projectAndReward
        .map(pr -> isSelectable(pr.first, pr.second));

      final Observable<Boolean> horizontalRewardsEnabled = Observable.just(environment.horizontalRewardsEnabled().get());

      final Observable<Reward> reward = this.projectAndReward
        .map(pr -> pr.second);

      final Observable<Boolean> rewardIsSelected = this.projectAndReward
        .map(pr -> BackingUtils.isBacked(pr.first, pr.second));

      // Hide 'all gone' header if limit has not been reached, or reward has been backed by user.
      this.allGoneTextViewIsGone = this.projectAndReward
        .map(pr -> !RewardUtils.isLimitReached(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
        .distinctUntilChanged();

      this.backersTextViewIsGone = reward
        .map(r -> RewardUtils.isNoReward(r) || !RewardUtils.hasBackers(r))
        .distinctUntilChanged();

      this.backersTextViewText = reward
        .filter(r -> RewardUtils.isReward(r) || RewardUtils.hasBackers(r))
        .map(Reward::backersCount)
        .filter(ObjectUtils::isNotNull);

      this.conversionTextViewIsGone = this.projectAndReward
        .map(p -> !p.first.currency().equals(p.first.currentCurrency()))
        .map(BooleanUtils::negate);

      this.conversionTextViewText = this.projectAndReward
        .map(pr -> this.ksCurrency.formatWithUserPreference(pr.second.minimum(), pr.first, RoundingMode.UP));

      this.descriptionTextViewText = reward.map(Reward::description);

      this.estimatedDeliveryDateTextViewText = reward
        .map(Reward::estimatedDeliveryOn)
        .filter(ObjectUtils::isNotNull);

      this.estimatedDeliveryDateSectionIsGone = reward
        .map(Reward::estimatedDeliveryOn)
        .map(ObjectUtils::isNull)
        .distinctUntilChanged();

      this.isClickable = isSelectable.distinctUntilChanged();

      this.startCheckoutActivity = this.projectAndReward
        .compose(combineLatestPair(horizontalRewardsEnabled))
        .filter(prAndHorizontalRewards -> !prAndHorizontalRewards.second)
        .map(prAndHorizontalRewards -> prAndHorizontalRewards.first)
        .filter(pr -> isSelectable(pr.first, pr.second) && pr.first.isLive())
        .compose(takeWhen(this.rewardClicked));

      this.startBackingActivity = this.projectAndReward
        .compose(takeWhen(this.rewardClicked))
        .filter(pr -> ProjectUtils.isCompleted(pr.first) && BackingUtils.isBacked(pr.first, pr.second))
        .map(pr -> pr.first);

      this.limitAndBackersSeparatorIsGone = reward
        .map(r -> IntegerUtils.isNonZero(r.limit()) && IntegerUtils.isNonZero(r.backersCount()))
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      this.limitAndRemainingTextViewIsGone = reward
        .map(RewardUtils::isLimited)
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      this.limitAndRemainingTextViewText = reward
        .map(r -> Pair.create(r.limit(), r.remaining()))
        .filter(lr -> lr.first != null && lr.second != null)
        .map(rr -> Pair.create(NumberUtils.format(rr.first), NumberUtils.format(rr.second)));

      // Hide limit header if reward is not limited, or reward has been backed by user.
      this.limitHeaderIsGone = this.projectAndReward
        .map(pr -> !RewardUtils.isLimited(pr.second) || BackingUtils.isBacked(pr.first, pr.second))
        .distinctUntilChanged();

      this.minimumTextViewText = formattedMinimum;

      this.rewardsItemList = reward
        .map(Reward::rewardsItems)
        .compose(coalesce(new ArrayList<>()));

      this.rewardsItemsAreGone = reward
        .map(RewardUtils::isItemized)
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      this.selectedHeaderIsGone = rewardIsSelected
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      this.shippingSummaryTextViewText = reward
        .filter(RewardUtils::isShippable)
        .map(Reward::shippingSummary);

      this.shippingSummarySectionIsGone = reward
        .map(RewardUtils::isShippable)
        .map(BooleanUtils::negate)
        .distinctUntilChanged();

      this.titleTextViewIsGone = reward
        .map(Reward::title)
        .map(ObjectUtils::isNull);

      this.rewardDescriptionIsGone = reward
        .map(Reward::description)
        .map(String::isEmpty);

      this.titleTextViewText = reward
        .map(Reward::title)
        .filter(ObjectUtils::isNotNull);

      this.whiteOverlayIsInvisible = this.projectAndReward
        .map(pr -> RewardUtils.isLimitReached(pr.second) && !BackingUtils.isBacked(pr.first, pr.second))
        .map(BooleanUtils::negate)
        .distinctUntilChanged();
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

    private final Observable<Boolean> allGoneTextViewIsGone;
    private final Observable<Boolean> backersTextViewIsGone;
    private final Observable<Integer> backersTextViewText;
    private final Observable<String> conversionTextViewText;
    private final Observable<Boolean> conversionTextViewIsGone;
    private final Observable<String> descriptionTextViewText;
    private final Observable<DateTime> estimatedDeliveryDateTextViewText;
    private final Observable<Boolean> estimatedDeliveryDateSectionIsGone;
    private final Observable<Boolean> isClickable;
    private final Observable<Boolean> limitAndBackersSeparatorIsGone;
    private final Observable<Boolean> limitAndRemainingTextViewIsGone;
    private final Observable<Pair<String, String>> limitAndRemainingTextViewText;
    private final Observable<Boolean> limitHeaderIsGone;
    private final Observable<String> minimumTextViewText;
    private final Observable<Boolean> rewardDescriptionIsGone;
    private final Observable<List<RewardsItem>> rewardsItemList;
    private final Observable<Boolean> rewardsItemsAreGone;
    private final Observable<Boolean> titleTextViewIsGone;
    private final Observable<String> titleTextViewText;
    private final Observable<Boolean> selectedHeaderIsGone;
    private final Observable<Boolean> shippingSummarySectionIsGone;
    private final Observable<String> shippingSummaryTextViewText;
    private final Observable<Project> startBackingActivity;
    private final Observable<Pair<Project, Reward>> startCheckoutActivity;
    private final Observable<Boolean> whiteOverlayIsInvisible;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void projectAndReward(final @NonNull Project project, final @NonNull Reward reward) {
      this.projectAndReward.onNext(Pair.create(project, reward));
    }
    @Override public void rewardClicked() {
      this.rewardClicked.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> allGoneTextViewIsGone() {
      return this.allGoneTextViewIsGone;
    }
    @Override public @NonNull Observable<Boolean> backersTextViewIsGone() {
      return this.backersTextViewIsGone;
    }
    @Override public @NonNull Observable<Integer> backersTextViewText() {
      return this.backersTextViewText;
    }
    @Override public @NonNull Observable<Boolean> conversionTextViewIsGone() {
      return this.conversionTextViewIsGone;
    }
    @Override public @NonNull Observable<String> conversionTextViewText() {
      return this.conversionTextViewText;
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
    @Override public @NonNull Observable<Boolean> estimatedDeliveryDateSectionIsGone() {
      return this.estimatedDeliveryDateSectionIsGone;
    }
    @Override public @NonNull Observable<Boolean> limitAndBackersSeparatorIsGone() {
      return this.limitAndBackersSeparatorIsGone;
    }
    @Override public @NonNull Observable<Boolean> limitAndRemainingTextViewIsGone() {
      return this.limitAndRemainingTextViewIsGone;
    }
    @Override public @NonNull Observable<Pair<String, String>> limitAndRemainingTextViewText() {
      return this.limitAndRemainingTextViewText;
    }
    @Override public @NonNull Observable<Boolean> limitHeaderIsGone() {
      return this.limitHeaderIsGone;
    }
    @Override public @NonNull Observable<String> minimumTextViewText() {
      return this.minimumTextViewText;
    }
    @Override public @NonNull Observable<Boolean> rewardDescriptionIsGone() {
      return this.rewardDescriptionIsGone;
    }
    @Override public @NonNull Observable<List<RewardsItem>> rewardsItemList() {
      return this.rewardsItemList;
    }
    @Override public @NonNull Observable<Boolean> rewardsItemsAreGone() {
      return this.rewardsItemsAreGone;
    }
    @Override public @NonNull Observable<Boolean> selectedHeaderIsGone() {
      return this.selectedHeaderIsGone;
    }
    @Override public @NonNull Observable<Boolean> shippingSummarySectionIsGone() {
      return this.shippingSummarySectionIsGone;
    }
    @Override public @NonNull Observable<String> shippingSummaryTextViewText() {
      return this.shippingSummaryTextViewText;
    }
    @Override public @NonNull Observable<Project> startBackingActivity() {
      return this.startBackingActivity;
    }
    @Override public @NonNull Observable<Pair<Project, Reward>> startCheckoutActivity() {
      return this.startCheckoutActivity;
    }
    @Override public @NonNull Observable<Boolean> titleTextViewIsGone() {
      return this.titleTextViewIsGone;
    }
    @Override public @NonNull Observable<String> titleTextViewText() {
      return this.titleTextViewText;
    }
    @Override public @NonNull Observable<Boolean> whiteOverlayIsInvisible() {
      return this.whiteOverlayIsInvisible;
    }
  }
}
