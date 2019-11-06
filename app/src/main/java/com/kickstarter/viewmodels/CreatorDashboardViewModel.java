package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.BooleanUtils;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CreatorDashboardActivity;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;
import com.kickstarter.ui.adapters.data.ProjectDashboardData;

import java.util.List;

import androidx.annotation.NonNull;
import rx.Notification;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.values;


public interface CreatorDashboardViewModel {
  interface Inputs extends CreatorDashboardBottomSheetAdapter.Delegate, CreatorDashboardAdapter.Delegate {
    /** Call when project selection should be shown. */
    void projectsListButtonClicked();
  }

  interface Outputs {
    /** Emits when the bottom sheet should be expanded. */
    Observable<Void> openBottomSheet();

    /** Emits a boolean determining if the progress bar should be visible. */
    Observable<Boolean> progressBarIsVisible();

    /** Emits the current project dashboard data. */
    Observable<ProjectDashboardData> projectDashboardData();

    /** Emits when project dropdown should be shown. */
    Observable<List<Project>> projectsForBottomSheet();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      this.client = environment.apiClient();

      final Observable<Boolean> intentHasProjectExtra = intent()
        .map(intent -> intent.hasExtra(IntentKey.PROJECT));

      final Observable<Notification<ProjectsEnvelope>> projectsNotification = intentHasProjectExtra
        .filter(BooleanUtils::isFalse)
        .switchMap(i -> this.client.fetchProjects(true)
          .materialize()
          .share());

      final Observable<ProjectsEnvelope> projectsEnvelope = projectsNotification
        .compose(values());

      final Observable<List<Project>> projects = projectsEnvelope
        .map(ProjectsEnvelope::projects);

      final Observable<Project> firstProject = projects
        .map(ListUtils::first);

      final Observable<Project> intentProject = intent()
        .map(intent -> intent.getParcelableExtra(IntentKey.PROJECT))
        .filter(ObjectUtils::isNotNull)
        .ofType(Project.class);

      final Observable<Project> currentProject = Observable
        .merge(firstProject, this.projectSelectionInput, intentProject)
        .filter(ObjectUtils::isNotNull);

      final Observable<Notification<ProjectStatsEnvelope>> projectStatsEnvelopeNotification = currentProject
        .switchMap(project -> this.client.fetchProjectStats(project)
          .doOnSubscribe(() -> this.progressBarIsVisible.onNext(true))
          .doAfterTerminate(() -> this.progressBarIsVisible.onNext(false)))
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

      Observable.combineLatest(currentProject, projectStatsEnvelope, intentHasProjectExtra, ProjectDashboardData::new)
        .compose(bindToLifecycle())
        .distinctUntilChanged()
        .subscribe(this.projectDashboardData);

      this.projectsListButtonClicked
        .compose(bindToLifecycle())
        .subscribe(this.openBottomSheet);

      this.projectSelectionInput
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackSwitchedProjects);

      this.projectsListButtonClicked
        .compose(bindToLifecycle())
        .subscribe(__ -> this.koala.trackOpenedProjectSwitcher());

      currentProject
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedProjectDashboard);
    }

    private final PublishSubject<Project> projectSelectionInput = PublishSubject.create();
    private final PublishSubject<Void> projectsListButtonClicked = PublishSubject.create();

    private final BehaviorSubject<Void> openBottomSheet = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> progressBarIsVisible = BehaviorSubject.create();
    private final BehaviorSubject<ProjectDashboardData> projectDashboardData = BehaviorSubject.create();
    private final Observable<List<Project>> projectsForBottomSheet;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void projectSelectionInput(final @NonNull Project project) {
      this.projectSelectionInput.onNext(project);
    }
    @Override
    public void projectsListButtonClicked() {
      this.projectsListButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Void> openBottomSheet() {
      return this.openBottomSheet;
    }
    @Override public Observable<Boolean> progressBarIsVisible() {
      return this.progressBarIsVisible;
    }
    @Override public @NonNull Observable<ProjectDashboardData> projectDashboardData() {
      return this.projectDashboardData;
    }
    @Override public @NonNull Observable<List<Project>> projectsForBottomSheet() {
      return this.projectsForBottomSheet;
    }

  }
}
