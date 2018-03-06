package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ReferrerType;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakdownViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardReferrerBreakdownHolderViewModel {

  interface Inputs {
    /** current project and related stats object **/
    void projectAndStatsInput(Pair<Project, ProjectStatsEnvelope> projectAndStats);
  }

  interface Outputs {
    /** Emits the percentage of total pledges from a custom referrer **/
    Observable<Float> customReferrerPercent();

    /** Emits the text for the percentage of total pledges from a custom referrer **/
    Observable<String> customReferrerPercentText();

    /** Emits the percentage of total pledges from a external referrer **/
    Observable<Float> externalReferrerPercent();

    /** Emits the text for the percentage of total pledges from an external referrer **/
    Observable<String> externalReferrerPercentText();

    /** Emits the percentage of total pledges from an internal referrer **/
    Observable<Float> internalReferrerPercent();

    /** Emits the text for the percentage of total pledges from an internal referrer **/
    Observable<String> internalReferrerPercentText();

    /** Emits a boolean that determines in the pledged via custom layout is gone **/
    Observable<Boolean> pledgedViaCustomLayoutIsGone();

    /** Emits a boolean that determines in the pledged via external layout is gone **/
    Observable<Boolean> pledgedViaExternalLayoutIsGone();

    /** Emits a boolean that determines in the pledged via internal layout is gone **/
    Observable<Boolean> pledgedViaInternalLayoutIsGone();

    /** Emits the current project and the average pledge for that project **/
    Observable<Pair<Project, Integer>> projectAndAveragePledge();

    /** Emits the current project and the amount pledged via custom referrers **/
    Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount();

    /** Emits the current project and the amount pledged via external referrers **/
    Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount();

    /** Emits the current project and the amount pledged via internal referrers **/
    Observable<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerBreakdownViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<Project> currentProject = this.projectAndProjectStatsInput
        .map(PairUtils::first);

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> referrerStats = this.projectAndProjectStatsInput
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope::referralDistribution);

      final Observable<ProjectStatsEnvelope.CumulativeStats> cumulativeStats = this.projectAndProjectStatsInput
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope::cumulative);

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> internalReferrers = referrerStats
        .flatMap(rs ->
          Observable.from(rs).filter(r -> r.referrerTypeEnumType() == ReferrerType.INTERNAL).toList()
        );

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> externalReferrers = referrerStats
        .flatMap(rs ->
          Observable.from(rs).filter(r -> r.referrerTypeEnumType() == ReferrerType.EXTERNAL).toList()
        );

      final Observable<List<ProjectStatsEnvelope.ReferrerStats>> customReferrers = referrerStats
        .flatMap(rs ->
          Observable.from(rs).filter(r -> r.referrerTypeEnumType() == ReferrerType.CUSTOM).toList()
        );

      final Observable<Integer> averagePledge = cumulativeStats
        .map(ProjectStatsEnvelope.CumulativeStats::averagePledge)
        .map(Float::intValue);

      this.projectAndAveragePledge = Observable.combineLatest(currentProject, averagePledge, Pair::create);

      this.customReferrerPercent = customReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.customReferrerPercentText = this.customReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.customReferrerPledgedAmount = customReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.pledged())
        );

      this.projectAndCustomReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.customReferrerPledgedAmount,
        Pair::create
      );

      this.externalReferrerPercent = externalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.externalReferrerPercentText = this.externalReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.externalReferrerPledgedAmount = externalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.pledged())
        );

      this.projectAndExternalReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.externalReferrerPledgedAmount,
        Pair::create
      );

      this.internalReferrerPercent = internalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.internalReferrerPercentText = this.internalReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage(percent * 100f));

      this.internalReferrerPledgedAmount = internalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.pledged())
        );

      this.projectAndInternalReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.internalReferrerPledgedAmount,
        Pair::create
      );

      this.pledgedViaCustomLayoutIsGone = this.customReferrerPledgedAmount
        .map(amount -> amount <= 0f);

      this.pledgedViaExternalLayoutIsGone = this.externalReferrerPledgedAmount
        .map(amount -> amount <= 0f);

      this.pledgedViaInternalLayoutIsGone = this.internalReferrerPledgedAmount
        .map(amount -> amount <= 0f);

      referrerStats
        .map(rs -> rs.size() > 10)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.referrersTitleIsLimitedCopy);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope>> projectAndProjectStatsInput = PublishSubject.create();

    private final Observable<Float> customReferrerPercent;
    private final Observable<String> customReferrerPercentText;
    private final Observable<Float> customReferrerPledgedAmount;
    private final Observable<Float> externalReferrerPercent;
    private final Observable<String> externalReferrerPercentText;
    private final Observable<Float> externalReferrerPledgedAmount;
    private final Observable<Float> internalReferrerPercent;
    private final Observable<String> internalReferrerPercentText;
    private final Observable<Float> internalReferrerPledgedAmount;
    private final Observable<Boolean> pledgedViaCustomLayoutIsGone;
    private final Observable<Boolean> pledgedViaExternalLayoutIsGone;
    private final Observable<Boolean> pledgedViaInternalLayoutIsGone;
    private final Observable<Pair<Project, Integer>> projectAndAveragePledge;
    private final Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount;
    private final PublishSubject<Boolean> referrersTitleIsLimitedCopy = PublishSubject.create();

    @Override
    public void projectAndStatsInput(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
      this.projectAndProjectStatsInput.onNext(projectAndStats);
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
    public @NonNull Observable<Float> externalReferrerPercent() {
      return this.externalReferrerPercent;
    }
    @Override
    public @NonNull Observable<String> externalReferrerPercentText() {
      return this.externalReferrerPercentText;
    }
    @Override
    public @NonNull Observable<Float> internalReferrerPercent() {
      return this.internalReferrerPercent;
    }
    @Override
    public @NonNull Observable<String> internalReferrerPercentText() {
      return this.internalReferrerPercentText;
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
    public @NonNull Observable<Boolean> pledgedViaInternalLayoutIsGone() {
      return this.pledgedViaInternalLayoutIsGone;
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
    public @NonNull Observable<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount() {
      return this.projectAndInternalReferrerPledgedAmount;
    }
  }
}
