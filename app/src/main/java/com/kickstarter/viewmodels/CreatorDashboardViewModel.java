package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.activities.CreatorDashboardActivity;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.values;


public interface CreatorDashboardViewModel {
  interface Inputs extends CreatorDashboardBottomSheetAdapter.Delegate {
    /** Call when a project is clicked to pass to delegate. */
    void projectSelectionInput(Project project);
  }

  interface Outputs {
    /** Emits current project and associated stats object. */
    Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats();

    /** Emits when project dropdown should be shown. */
    Observable<List<Project>> projectsForBottomSheet();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      this.client = environment.apiClient();

      final Observable<Notification<ProjectsEnvelope>> projectsNotification =
        this.client.fetchProjects(true)
          .materialize()
          .share();

      final Observable<ProjectsEnvelope> projectsEnvelope = projectsNotification
        .compose(values());

      final Observable<List<Project>> projects = projectsEnvelope
        .map(ProjectsEnvelope::projects);

      final Observable<Project> firstProject = projects
        .map(ListUtils::first);

      final Observable<Project> currentProject = Observable
        .merge(firstProject, this.projectSelectionInput)
        .filter(ObjectUtils::isNotNull);

      final Observable<Notification<ProjectStatsEnvelope>> projectStatsEnvelopeNotification = currentProject
        .switchMap(this.client::fetchProjectStats)
        .share()
        .materialize();

      final Observable<ProjectStatsEnvelope> projectStatsEnvelope = projectStatsEnvelopeNotification
        .compose(values());

      this.projectsForBottomSheet = Observable.combineLatest(
        projects.filter(projectList -> projectList.size() > 1),
        currentProject,
        (projectList, project) -> Observable
          .from(projectList)
          .filter(p -> p.id() != project.id())
          .toList())
        .flatMap(listObservable -> listObservable);

      currentProject
        .compose(combineLatestPair(projectStatsEnvelope))
        .compose(bindToLifecycle())
        .distinctUntilChanged()
        .subscribe(this.projectAndStats);

      this.projectSelectionInput
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackSwitchedProjects);

      currentProject
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedProjectDashboard);
    }

    private final PublishSubject<Project> projectSelectionInput = PublishSubject.create();

    private final BehaviorSubject<Pair<Project, ProjectStatsEnvelope>> projectAndStats = BehaviorSubject.create();
    private final Observable<List<Project>> projectsForBottomSheet;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void projectSelectionInput(final @NonNull Project project) {
      this.projectSelectionInput.onNext(project);
    }

    @Override public @NonNull Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats() {
      return this.projectAndStats;
    }
    @Override public @NonNull Observable<List<Project>> projectsForBottomSheet() {
      return this.projectsForBottomSheet;
    }

  }
}
