package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardReferrerStatsViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardReferrerStatsRowHolderViewModel {
  interface Inputs {
    void projectAndReferrerStatsInput(Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats);
  }

  interface Outputs {
    /* percent of the total that came from this referrer */
    Observable<String> percentageOfTotalPledged();

    /* project and the amount pledged from this referral source */
    Observable<Pair<Project, Float>> projectAndPledgedForReferrer();

    /* string number of backers */
    Observable<String> referrerBackerCount();

    /* name of the source of referrals */
    Observable<String> referrerSourceName();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardReferrerStatsViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.percentageOfTotalPledged = projectAndReferrerStats
        .map(projectAndReferrerStats -> {
          final Project p = projectAndReferrerStats.first;
          final ProjectStatsEnvelope.ReferrerStats rs = projectAndReferrerStats.second;
          return NumberUtils.flooredPercentage((rs.pledged() / p.pledged()) * 100);
        })
        .map(StringUtils::wrapInParentheses);

      this.projectAndPledgedForReferrer = projectAndReferrerStats
        .map(pr -> Pair.create(pr.first, (float) pr.second.pledged()));

      this.referrerSourceName = projectAndReferrerStats
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope.ReferrerStats::referrerName);

      this.referrerBackerCount = projectAndReferrerStats
        .map(PairUtils::second)
        .map(ProjectStatsEnvelope.ReferrerStats::backersCount)
        .map(NumberUtils::format);
    }
    
    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats = PublishSubject.create();

    private final Observable<String> percentageOfTotalPledged;
    private final Observable<Pair<Project, Float>> projectAndPledgedForReferrer;
    private final Observable<String> referrerBackerCount;
    private final Observable<String> referrerSourceName;
    @Override
    public void projectAndReferrerStatsInput(final @NonNull Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats) {
      this.projectAndReferrerStats.onNext(projectAndReferrerStats);
    }

    @Override public @NonNull Observable<String> percentageOfTotalPledged() {
      return this.percentageOfTotalPledged;
    }
    @Override public @NonNull Observable<Pair<Project, Float>> projectAndPledgedForReferrer() {
      return this.projectAndPledgedForReferrer;
    }
    @Override public @NonNull Observable<String> referrerBackerCount() {
      return this.referrerBackerCount;
    }
    @Override public @NonNull Observable<String> referrerSourceName() {
      return this.referrerSourceName;
    }
  }

}
