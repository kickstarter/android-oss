package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardReferrerStatsRowHolderViewModel {
  interface Inputs {
    /** Current project and list of referrer stats. */
    void projectAndReferrerStatsInput(Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats);
  }

  interface Outputs {
    /** Emits project and the amount pledged for this referrer. */
    Observable<Pair<Project, Float>> projectAndPledgedForReferrer();

    /** Emits string number of backers. */
    Observable<String> referrerBackerCount();

    /** Emits resource ID of referrer color. */
    Observable<Integer> referrerSourceColorId();

    /** Emits source name of referrer. */
    Observable<String> referrerSourceName();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerStatsViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectAndPledgedForReferrer = this.projectAndReferrerStats
        .map(pr -> Pair.create(pr.first, pr.second.pledged()));

      this.referrerSourceColorId = this.projectAndReferrerStats
        .map(PairUtils::second)
        .map(referrerStat -> ProjectStatsEnvelope.ReferrerStats.referrerTypeEnum(referrerStat.referrerType()).getReferrerColorId());

      this.referrerSourceName = this.projectAndReferrerStats
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope.ReferrerStats::referrerName);

      this.referrerBackerCount = this.projectAndReferrerStats
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope.ReferrerStats::backersCount)
        .map(NumberUtils::format);
    }
    
    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats = PublishSubject.create();

    private final Observable<Pair<Project, Float>> projectAndPledgedForReferrer;
    private final Observable<String> referrerBackerCount;
    private final Observable<Integer> referrerSourceColorId;
    private final Observable<String> referrerSourceName;

    @Override public void projectAndReferrerStatsInput(final @NonNull Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats) {
      this.projectAndReferrerStats.onNext(projectAndReferrerStats);
    }

    @Override public @NonNull Observable<Pair<Project, Float>> projectAndPledgedForReferrer() {
      return this.projectAndPledgedForReferrer;
    }
    @Override public @NonNull Observable<String> referrerBackerCount() {
      return this.referrerBackerCount;
    }
    @Override public @NonNull Observable<Integer> referrerSourceColorId() {
      return this.referrerSourceColorId;
    }
    @Override public @NonNull Observable<String> referrerSourceName() {
      return this.referrerSourceName;
    }
  }
}
