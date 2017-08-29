package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface CreatorDashboardHeaderHolderViewModel {

  interface Inputs {
    void projectAndStats(Pair<Project, ProjectStatsEnvelope> projectAndProjectStatsEnvelope);
    void projectViewClicked();
  }

  interface Outputs {
    /* project that is currently being viewed */
    Observable<Project> currentProject();

    /* string number with the percentage of a projects funding */
    Observable<String> percentageFunded();

    /* localized count of number of backers */
    Observable<String> projectBackersCountText();

    /* current projects blurb */
    Observable<String> projectBlurbTextViewText();

    /* current project's name */
    Observable<String> projectNameTextViewText();

    /* time remaining for latest project (no units) */
    Observable<String> timeRemainingText();

    /* call when button is clicked to view individual project page */
    Observable<Pair<Project, RefTag>> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.currentProject = this.projectAndStats
        .map(PairUtils::first);

      this.percentageFunded = this.projectAndStats
        .map(PairUtils::first)
        .map(p -> NumberUtils.flooredPercentage(p.percentageFunded()));

      this.projectBlurbTextViewText = this.projectAndStats
        .map(PairUtils::first)
        .map(Project::blurb)
        .compose(bindToLifecycle());

      this.projectBackersCountText = this.projectAndStats
        .map(PairUtils::first)
        .map(Project::backersCount)
        .map(NumberUtils::format)
        .compose(bindToLifecycle());

      this.projectNameTextViewText = this.projectAndStats
        .map(PairUtils::first)
        .map(Project::name)
        .distinctUntilChanged()
        .compose(bindToLifecycle());

      this.timeRemainingText = this.projectAndStats
        .map(PairUtils::first)
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format);

      this.startProjectActivity = this.currentProject()
        .compose(takeWhen(this.projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()));
    }


    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope>> projectAndStats = PublishSubject.create();
    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();

    private final Observable<String> percentageFunded;
    private final Observable<Project> currentProject;
    private final Observable<String> projectBackersCountText;
    private final Observable<String> projectBlurbTextViewText;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<String> timeRemainingText;

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override
    public void projectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndProjectStatsEnvelope) {
      this.projectAndStats.onNext(projectAndProjectStatsEnvelope);
    }


    @Override public @NonNull Observable<String> percentageFunded() {
      return this.percentageFunded;
    }
    @Override public @NonNull Observable<Project> currentProject() {
      return this.currentProject;
    }
    @Override public @NonNull Observable<String> projectBackersCountText() {
      return this.projectBackersCountText;
    }
    @Override public @NonNull Observable<String> projectBlurbTextViewText() {
      return this.projectBlurbTextViewText;
    }
    @Override public @NonNull Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> timeRemainingText() {
      return this.timeRemainingText;
    }
  }
}
