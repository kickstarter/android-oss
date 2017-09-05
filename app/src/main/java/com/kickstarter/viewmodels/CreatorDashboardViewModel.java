package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.activities.CreatorDashboardActivity;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.combineLatestPair;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.values;


public interface CreatorDashboardViewModel {
  interface Inputs extends CreatorDashboardBottomSheetAdapter.Delegate {
    void projectViewClicked();
    void projectSwitcherProjectClickInput(Project project);
    void refreshProject(Project project);
  }

  interface Outputs {
    /* most recent project by the creator */
    Observable<Project> latestProject();

    /* project and associated stats object */
    Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats();

    /* emits when project dropdown should be shown */
    Observable<List<Project>> projectsForBottomSheet();

    /* emits when a project is clicked in the project switcher */
    Observable<Project> projectSwitcherProjectClickOutput();

    /* call when button is clicked to view individual project page */
    Observable<Pair<Project, RefTag>> startProjectActivity();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
      this.client = environment.apiClient();

      final Observable<Notification<ProjectsEnvelope>> projectsNotification =
        this.client.fetchProjects(true).materialize().share();

      final Observable<ProjectsEnvelope> projectsEnvelope = projectsNotification
        .compose(values());

      final Observable<List<Project>> projects = projectsEnvelope
        .map(ProjectsEnvelope::projects);

//      final Observable<Project> latestProject = projects
//        .map(ListUtils::first);

      projects.map(ListUtils::first).subscribe(this.currentProject::onNext);

      final Observable<Notification<ProjectStatsEnvelope>> projectStatsEnvelopeNotification = currentProject
        .switchMap(this.client::fetchProjectStats)
        .share()
        .materialize();

      final Observable<ProjectStatsEnvelope> projectStatsEnvelope = projectStatsEnvelopeNotification
        .compose(values());

      this.projectsForBottomSheet = projects;

      this.latestProject = currentProject;

      this.projectAndStats = currentProject
        .compose(combineLatestPair(projectStatsEnvelope));

      this.projectSwitcherProjectClickOutput = this.projectSwitcherClicked;


      this.startProjectActivity = currentProject
        .compose(takeWhen(this.projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()));
    }

    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();
    private final PublishSubject<Project> projectSwitcherClicked = PublishSubject.create();
    private final PublishSubject<Project> currentProject = PublishSubject.create();

    private final Observable<Project> latestProject;
    private final Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats;
    private final Observable<List<Project>> projectsForBottomSheet;
    private final Observable<Project> projectSwitcherProjectClickOutput;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override
    public void projectSwitcherProjectClickInput(final @NonNull Project project) {
      this.projectSwitcherClicked.onNext(project);
    }

    @Override
    public void refreshProject(final @NonNull Project project) {
      this.currentProject.onNext(project);
    }

    @Override public @NonNull Observable<Project> latestProject() {
      return this.latestProject;
    }
    @Override public @NonNull Observable<Pair<Project, ProjectStatsEnvelope>> projectAndStats() {
      return this.projectAndStats;
    }
    @Override public @NonNull Observable<List<Project>> projectsForBottomSheet() {
      return this.projectsForBottomSheet;
    }
    @Override public @NonNull Observable<Project> projectSwitcherProjectClickOutput() { return this.projectSwitcherProjectClickOutput; }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
  }
}
