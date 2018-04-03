package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakdownViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;

public interface CreatorDashboardReferrerBreakdownHolderViewModel {

  interface Inputs {
    /** current project and related stats object **/
    void projectAndStatsInput(Pair<Project, ProjectStatsEnvelope> projectAndStats);
  }

  interface Outputs {
    /** Emits a boolean that determines if breakdown chart is gone. */
    Observable<Boolean> breakdownViewIsGone();

    /** Emits the percentage of total pledges from a custom referrer. */
    Observable<Float> customReferrerPercent();

    /** Emits the text for the percentage of total pledges from a custom referrer. */
    Observable<String> customReferrerPercentText();

    /** Emits a boolean that determines if empty view is gone. */
    Observable<Boolean> emptyViewIsGone();

    /** Emits the percentage of total pledges from a external referrer. */
    Observable<Float> externalReferrerPercent();

    /** Emits the text for the percentage of total pledges from an external referrer. */
    Observable<String> externalReferrerPercentText();

    /** Emits the percentage of total pledges from a Kickstarter referrer. */
    Observable<Float> kickstarterReferrerPercent();

    /** Emits the text for the percentage of total pledges from a Kickstarter referrer. */
    Observable<String> kickstarterReferrerPercentText();

    /** Emits a boolean that determines if the pledged via custom layout is gone. */
    Observable<Boolean> pledgedViaCustomLayoutIsGone();

    /** Emits a boolean that determines if the pledged via external layout is gone. */
    Observable<Boolean> pledgedViaExternalLayoutIsGone();

    /** Emits a boolean that determines if the pledged via Kickstarter layout is gone. */
    Observable<Boolean> pledgedViaKickstarterLayoutIsGone();

    /** Emits the current project and the average pledge for that project. */
    Observable<Pair<Project, Integer>> projectAndAveragePledge();

    /** Emits the current project and the amount pledged via custom referrers. */
    Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount();

    /** Emits the current project and the amount pledged via external referrers. */
    Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount();

    /** Emits the current project and the amount pledged via Kickstarter referrers. */
    Observable<Pair<Project, Float>> projectAndKickstarterReferrerPledgedAmount();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerBreakdownViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Project> currentProject = this.projectAndProjectStatsInput
        .map(PairUtils::first);

      final Observable<ProjectStatsEnvelope> projectStats = this.projectAndProjectStatsInput
        .map(PairUtils::second);

      final Observable<ProjectStatsEnvelope.ReferralAggregateStats> referralAggregates = this.projectAndProjectStatsInput
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope::referralAggregates);

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> referrerStats = projectStats
        .map(ProjectStatsEnvelope::referralDistribution);

      final Observable<ProjectStatsEnvelope.CumulativeStats> cumulativeStats = projectStats
        .map(ProjectStatsEnvelope::cumulative);

      final Observable<Integer> averagePledge = cumulativeStats
        .map(ProjectStatsEnvelope.CumulativeStats::averagePledge)
        .map(Float::intValue);

      final Observable<Float> pledged = cumulativeStats
        .map(ProjectStatsEnvelope.CumulativeStats::pledged);

      this.projectAndAveragePledge = Observable.combineLatest(currentProject, averagePledge, Pair::create);

      final Observable<Boolean> emptyStats = referrerStats
        .map(List::isEmpty);

      this.breakdownViewIsGone = emptyStats;

      this.emptyViewIsGone = emptyStats
        .map(BooleanUtils::negate);

      this.customReferrerPercent = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::custom)
        .compose(combineLatestPair(pledged))
        .map(customAndPledged -> IntegerUtils.isZero(customAndPledged.second.intValue()) ?  0 : customAndPledged.first / customAndPledged.second);

      this.customReferrerPercentText = this.customReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.customReferrerPledgedAmount = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::custom);

      this.projectAndCustomReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.customReferrerPledgedAmount,
        Pair::create
      );

      this.externalReferrerPercent = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::external)
        .compose(combineLatestPair(pledged))
        .map(externalAndPledged -> IntegerUtils.isZero(externalAndPledged.second.intValue()) ?  0 : externalAndPledged.first / externalAndPledged.second);

      this.externalReferrerPercentText = this.externalReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.externalReferrerPledgedAmount = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::external);

      this.projectAndExternalReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.externalReferrerPledgedAmount,
        Pair::create
      );

      this.kickstarterReferrerPercent = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::internal)
        .compose(combineLatestPair(pledged))
        .map(internalAndPledged -> IntegerUtils.isZero(internalAndPledged.second.intValue()) ?  0 : internalAndPledged.first / internalAndPledged.second);

      this.kickstarterReferrerPercentText = this.kickstarterReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.kickstarterReferrerPledgedAmount = referralAggregates
        .map(ProjectStatsEnvelope.ReferralAggregateStats::internal);

      this.projectAndKickstarterReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.kickstarterReferrerPledgedAmount,
        Pair::create
      );

      this.pledgedViaCustomLayoutIsGone = this.customReferrerPledgedAmount
        .map(amount -> amount <= 0f);

      this.pledgedViaExternalLayoutIsGone = this.externalReferrerPledgedAmount
        .map(amount -> amount <= 0f);

      this.pledgedViaKickstarterLayoutIsGone = this.kickstarterReferrerPledgedAmount
        .map(amount -> amount <= 0f);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope>> projectAndProjectStatsInput = PublishSubject.create();

    private final Observable<Boolean> breakdownViewIsGone;
    private final Observable<Float> customReferrerPercent;
    private final Observable<String> customReferrerPercentText;
    private final Observable<Float> customReferrerPledgedAmount;
    private final Observable<Boolean> emptyViewIsGone;
    private final Observable<Float> externalReferrerPercent;
    private final Observable<String> externalReferrerPercentText;
    private final Observable<Float> externalReferrerPledgedAmount;
    private final Observable<Float> kickstarterReferrerPercent;
    private final Observable<String> kickstarterReferrerPercentText;
    private final Observable<Float> kickstarterReferrerPledgedAmount;
    private final Observable<Boolean> pledgedViaCustomLayoutIsGone;
    private final Observable<Boolean> pledgedViaExternalLayoutIsGone;
    private final Observable<Boolean> pledgedViaKickstarterLayoutIsGone;
    private final Observable<Pair<Project, Integer>> projectAndAveragePledge;
    private final Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndKickstarterReferrerPledgedAmount;

    @Override
    public void projectAndStatsInput(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
      this.projectAndProjectStatsInput.onNext(projectAndStats);
    }

    @Override
    public Observable<Boolean> breakdownViewIsGone() {
      return this.breakdownViewIsGone;
    }
    @Override
    public @NonNull Observable<Float> customReferrerPercent() {
      return this.customReferrerPercent;
    }
    @Override
    public @NonNull Observable<String> customReferrerPercentText() {
      return this.customReferrerPercentText;
    }
    @Override
    public @NonNull Observable<Boolean> emptyViewIsGone() {
      return this.emptyViewIsGone;
    }
    @Override
    public @NonNull Observable<Float> externalReferrerPercent() {
      return this.externalReferrerPercent;
    }
    @Override
    public @NonNull Observable<String> externalReferrerPercentText() {
      return this.externalReferrerPercentText;
    }
    @Override
    public @NonNull Observable<Float> kickstarterReferrerPercent() {
      return this.kickstarterReferrerPercent;
    }
    @Override
    public @NonNull Observable<String> kickstarterReferrerPercentText() {
      return this.kickstarterReferrerPercentText;
    }
    @Override
    public @NonNull Observable<Boolean> pledgedViaCustomLayoutIsGone() {
      return this.pledgedViaCustomLayoutIsGone;
    }
    @Override
    public @NonNull Observable<Boolean> pledgedViaExternalLayoutIsGone() {
      return this.pledgedViaExternalLayoutIsGone;
    }
    @Override
    public @NonNull Observable<Boolean> pledgedViaKickstarterLayoutIsGone() {
      return this.pledgedViaKickstarterLayoutIsGone;
    }
    @Override
    public @NonNull Observable<Pair<Project, Integer>> projectAndAveragePledge() {
      return this.projectAndAveragePledge;
    }
    @Override
    public @NonNull Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount() {
      return this.projectAndCustomReferrerPledgedAmount;
    }
    @Override
    public @NonNull Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount() {
      return this.projectAndExternalReferrerPledgedAmount;
    }
    @Override
    public @NonNull Observable<Pair<Project, Float>> projectAndKickstarterReferrerPledgedAmount() {
      return this.projectAndKickstarterReferrerPledgedAmount;
    }
  }
}
