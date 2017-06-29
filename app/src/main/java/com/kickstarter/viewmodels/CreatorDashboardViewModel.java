package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.activities.CreatorDashboardActivity;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.values;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;


public interface CreatorDashboardViewModel {
  interface Inputs {
    void projectViewClicked();
  }

  interface Outputs {
    /* most recent project by the creator */
    Observable<Project> latestProject();

    /* localized count of number of backers */
    Observable<String> projectBackersCountText();

    /* name of the latest project */
    Observable<String> projectNameTextViewText();

    /* call when button is clicked to view individual project page */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /* time remaining for latest project (no units) */
    Observable<String> timeRemaining();
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

      final Observable<Project> latestProject = projects
        .map(ListUtils::first);

      final Observable<ProjectStats> projectStatsNotification = latestProject
        .switchMap(this.client::fetchProjectStats);

      this.latestProject = latestProject;

      this.projectBackersCountText = latestProject
        .map(Project::backersCount)
        .map(NumberUtils::format);

      this.projectNameTextViewText = latestProject
        .map(Project::name);

      this.startProjectActivity = latestProject
        .compose(takeWhen(this.projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()));

      this.timeRemaining = latestProject
        .map(ProjectUtils::deadlineCountdownValue)
        .map(NumberUtils::format);
    }

    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();

    private final Observable<Project> latestProject;
    private final Observable<String> projectBackersCountText;
    private final Observable<String> projectNameTextViewText;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;
    private final Observable<String> timeRemaining;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override public @NonNull Observable<Project> latestProject() {
      return this.latestProject;
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
    @Override public @NonNull Observable<String> timeRemaining() {
      return this.timeRemaining;
    }
  }
}
