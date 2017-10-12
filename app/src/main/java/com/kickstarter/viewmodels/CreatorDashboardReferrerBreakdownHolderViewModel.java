package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ReferrerColor;
import com.kickstarter.libs.ReferrerType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerBreakDownViewHolder;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardReferrerBreakdownHolderViewModel {

  interface Inputs {
    void projectAndStatsInput(Pair<Project, ProjectStatsEnvelope> projectAndStats);
  }

  interface Outputs {
    Observable<Integer> customReferrerColor();
    Observable<Float> customReferrerPercent();
    Observable<String> customReferrerPercentText();
    Observable<Float> customReferrerPledgedAmount();
    Observable<Integer> externalReferrerColor();
    Observable<Float> externalReferrerPercent();
    Observable<String> externalReferrerPercentText();
    Observable<Integer> internalReferrerColor();
    Observable<Float> internalReferrerPercent();
    Observable<String> internalReferrerPercentText();
    Observable<Boolean> pledgedViaCustomLayoutIsGone();
    Observable<Boolean> pledgedViaExternalLayoutIsGone();
    Observable<Boolean> pledgedViaInternalLayoutIsGone();
    Observable<Pair<Project, Integer>> projectAndAveragePledge();
    Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount();
    Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount();
    Observable<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerBreakDownViewHolder> implements Inputs, Outputs {

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

      this.projectAndAveragePledge = cumulativeStats
        .map(ProjectStatsEnvelope.CumulativeStats::averagePledge)
        .map(Float::intValue)
        .compose(avg -> Observable.combineLatest(currentProject, avg, Pair::create));

      this.customReferrerColor = Observable.just(ReferrerColor.CUSTOM.getReferrerColor())
        .compose(Transformers.takeWhen(projectAndProjectStatsInput));

      this.customReferrerPercent = customReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.customReferrerPercentText = this.customReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage((percent.floatValue() * 100f)));

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

      this.externalReferrerColor = Observable.just(ReferrerColor.EXTERNAL.getReferrerColor())
        .compose(Transformers.takeWhen(projectAndProjectStatsInput));

      this.externalReferrerPercent = externalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.externalReferrerPercentText = externalReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage((percent * 100f)));

      this.externalReferrerPledgedAmount = customReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.pledged())
        );

      this.projectAndExternalReferrerPledgedAmount = Observable.combineLatest(
        currentProject,
        this.externalReferrerPledgedAmount,
        Pair::create
      );

      this.internalReferrerColor = Observable.just(ReferrerColor.INTERNAL.getReferrerColor())
        .compose(Transformers.takeWhen(projectAndProjectStatsInput));

      this.internalReferrerPercent = internalReferrers
        .flatMap(rs ->
          Observable.from(rs)
            .reduce(0f, (accum, stat) -> accum + stat.percentageOfDollars())
        );

      this.internalReferrerPercentText = internalReferrerPercent
        .map(percent -> NumberUtils.flooredPercentage((percent.floatValue() * 100f)));

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
        .map(amount -> amount > 0f)
        .map(BooleanUtils::negate);

      this.pledgedViaExternalLayoutIsGone = this.externalReferrerPledgedAmount
        .map(amount -> amount > 0f)
        .map(BooleanUtils::negate);

      this.pledgedViaInternalLayoutIsGone = this.internalReferrerPledgedAmount
        .map(amount -> amount > 0f)
        .map(BooleanUtils::negate);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject <Pair<Project, ProjectStatsEnvelope>> projectAndProjectStatsInput = PublishSubject.create();

    private final Observable<Pair<Project, Integer>> projectAndAveragePledge;
    private final Observable<Integer> customReferrerColor;
    private final Observable<Float> customReferrerPercent;
    private final Observable<String> customReferrerPercentText;
    private final Observable<Float> customReferrerPledgedAmount;
    private final Observable<Integer> externalReferrerColor;
    private final Observable<Float> externalReferrerPercent;
    private final Observable<String> externalReferrerPercentText;
    private final Observable<Float> externalReferrerPledgedAmount;
    private final Observable<Integer> internalReferrerColor;
    private final Observable<Float> internalReferrerPercent;
    private final Observable<String> internalReferrerPercentText;
    private final Observable<Float> internalReferrerPledgedAmount;
    private final Observable<Boolean> pledgedViaCustomLayoutIsGone;
    private final Observable<Boolean> pledgedViaExternalLayoutIsGone;
    private final Observable<Boolean> pledgedViaInternalLayoutIsGone;
    private final Observable<Pair<Project, Float>> projectAndCustomReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndExternalReferrerPledgedAmount;
    private final Observable<Pair<Project, Float>> projectAndInternalReferrerPledgedAmount;


    @Override
    public void projectAndStatsInput(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
      this.projectAndProjectStatsInput.onNext(projectAndStats);
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
    @Override
    public @NonNull Observable<Pair<Project, Integer>> projectAndAveragePledge() {
      return this.projectAndAveragePledge;
    }
    @Override
    public @NonNull Observable<Integer> customReferrerColor() {
      return this.customReferrerColor;
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
    public @NonNull Observable<Float> customReferrerPledgedAmount() {
      return this.customReferrerPledgedAmount;
    }
    @Override
    public @NonNull Observable<Integer> externalReferrerColor() {
      return this.externalReferrerColor;
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
    public @NonNull Observable<Integer> internalReferrerColor() {
      return this.internalReferrerColor;
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
  }
}
