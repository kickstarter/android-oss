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
    /** Call when the back button is clicked and the bottom sheet is expanded. */
    void backClicked();

    /** Call when project selection should be shown. */
    void projectsListButtonClicked();

    /** Call when the scrim is clicked. */
    void scrimClicked();
  }

  interface Outputs {
    /** Emits a boolean determining if the bottom sheet should expand. */
    Observable<Boolean> bottomSheetShouldExpand();

    /** Emits a boolean determining if the progress bar should be visible. */
    Observable<Boolean> progressBarIsVisible();

    /** Emits the current project dashboard data. */
    Observable<ProjectDashboardData> projectDashboardData();

    /** Emits the current project's name. */
    Observable<String> projectName();

    /** Emits when project dropdown should be shown. */
    Observable<List<Project>> projectsForBottomSheet();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      this.client = environment.apiClient();

      final Observable<Boolean> isViewingSingleProject = intent()
        .map(intent -> intent.hasExtra(IntentKey.PROJECT));

      final Observable<Notification<ProjectsEnvelope>> projectsNotification = isViewingSingleProject
        .filter(BooleanUtils::isFalse)
        .switchMap(i -> this.client.fetchProjects(true)
          .materialize()
          .share());

      final Observable<List<Project>> projects = projectsNotification
        .compose(values())
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

      currentProject
        .map(Project::name)
        .compose(bindToLifecycle())
        .subscribe(this.projectName);

      final Observable<Notification<ProjectStatsEnvelope>> projectStatsEnvelopeNotification = currentProject
        .switchMap(project -> this.client.fetchProjectStats(project)
          .doOnSubscribe(() -> this.progressBarIsVisible.onNext(true))
          .doAfterTerminate(() -> this.progressBarIsVisible.onNext(false)))
        .share()
        .materialize();

      final Observable<ProjectStatsEnvelope> projectStatsEnvelope = projectStatsEnvelopeNotification
        .compose(values());

      Observable.combineLatest(
        projects.filter(projectList -> projectList.size() > 1),
        currentProject,
        (projectList, project) -> Observable
          .from(projectList)
          .filter(p -> p.id() != project.id())
          .toList())
        .flatMap(listObservable -> listObservable)
        .compose(bindToLifecycle())
        .subscribe(this.projectsForBottomSheet);

      Observable.combineLatest(currentProject, projectStatsEnvelope, isViewingSingleProject, ProjectDashboardData::new)
        .compose(bindToLifecycle())
        .distinctUntilChanged()
        .subscribe(this.projectDashboardData);

      this.projectsListButtonClicked
        .map(__ -> true)
        .compose(bindToLifecycle())
        .subscribe(this.bottomSheetShouldExpand);

      Observable.merge(this.backClicked, this.scrimClicked, this.projectSelectionInput)
        .map(__ -> false)
        .compose(bindToLifecycle())
        .subscribe(this.bottomSheetShouldExpand);

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

    private final PublishSubject<Void> backClicked = PublishSubject.create();
    private final PublishSubject<Project> projectSelectionInput = PublishSubject.create();
    private final PublishSubject<Void> projectsListButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> scrimClicked = PublishSubject.create();

    private final BehaviorSubject<Boolean> bottomSheetShouldExpand = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> progressBarIsVisible = BehaviorSubject.create();
    private final BehaviorSubject<ProjectDashboardData> projectDashboardData = BehaviorSubject.create();
    private final BehaviorSubject<List<Project>> projectsForBottomSheet = BehaviorSubject.create();
    private final BehaviorSubject<String> projectName = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;


    @Override public void backClicked() {
      this.backClicked.onNext(null);
    }
    @Override public void projectSelectionInput(final @NonNull Project project) {
      this.projectSelectionInput.onNext(project);
    }
    @Override public void projectsListButtonClicked() {
      this.projectsListButtonClicked.onNext(null);
    }
    @Override public void scrimClicked() {
      this.scrimClicked.onNext(null);
    }

    @Override public @NonNull Observable<Boolean> bottomSheetShouldExpand() {
      return this.bottomSheetShouldExpand;
    }
    @Override public Observable<Boolean> progressBarIsVisible() {
      return this.progressBarIsVisible;
    }
    @Override public @NonNull Observable<ProjectDashboardData> projectDashboardData() {
      return this.projectDashboardData;
    }
    @Override public @NonNull Observable<String> projectName() {
      return this.projectName;
    }
    @Override public @NonNull Observable<List<Project>> projectsForBottomSheet() {
      return this.projectsForBottomSheet;
    }
  }
}
