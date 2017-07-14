package com.kickstarter.viewmodels;


import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.PairUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStatsEnvelope;
import com.kickstarter.ui.viewholders.CreatorDashboardHeaderViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardHeaderHolderViewModel {

  interface Inputs {
    void projectAndStats(Project project, ProjectStatsEnvelope projectStats);
    void projectViewClicked();
  }

  interface Outputs {
    /* string number with the percentage of a projects funding */
    Observable<String> percentageFunded();

    /* localized count of number of backers */
    Observable<String> projectBackersCountText();

    /* current project's name */
    Observable<String> projectNameTextViewText();

    /* project that is currently being viewed */
    Observable<Project> currentProject();

    /* time remaining for latest project (no units) */
    Observable<String> timeRemainingText();

    /* call when button is clicked to view individual project page */
    Observable<Pair<Project, RefTag>> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardHeaderViewHolder> implements
    Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.currentProject =  projectAndStats
        .map(PairUtils::first);

      this.percentageFunded = projectAndStats
        .map(PairUtils::first)
        .map(Project::percentageFunded)
        .map(NumberUtils::flooredPercentage)
        .compose(bindToLifecycle());

      this.projectBackersCountText = projectAndStats
        .map(PairUtils::first)
        .map(Project::backersCount)
        .map(NumberUtils::format)
        .compose(bindToLifecycle());

      this.projectNameTextViewText = projectAndStats
        .map(PairUtils::first)
        .map(Project::name)
        .distinctUntilChanged()
        .compose(bindToLifecycle());

      this.timeRemainingText = projectAndStats
        .map(PairUtils::first)
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format);

      this.startProjectActivity = this.currentProject()
        .compose(Transformers.takeWhen(projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()));
    }


    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Pair<Project, ProjectStatsEnvelope>> projectAndStats = PublishSubject.create();
    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();

    private final Observable<String> percentageFunded;
    private final Observable<Project> currentProject;
    private final Observable<String> projectBackersCountText;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<String> timeRemainingText;

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override
    public void projectAndStats(final @NonNull Project project, final @NonNull ProjectStatsEnvelope ProjectStatsEnvelope) {
      this.projectAndStats.onNext(Pair.create(project, ProjectStatsEnvelope));
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
