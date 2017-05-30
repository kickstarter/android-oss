package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ProjectsEnvelope;
import com.kickstarter.ui.activities.CreatorDashboardActivity;

import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardViewModel {
  interface Inputs {
    void projectViewClicked();
  }

  interface Outputs {

    Observable<String> projectNameTextViewText();

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
        .compose(Transformers.values());

      final Observable<List<Project>> projects = projectsEnvelope
        .map(ProjectsEnvelope::projects);

      final Observable<Project> latestProject = projects
        .map(ListUtils::first);

      this.projectNameTextViewText = latestProject
        .map(Project::name);

      this.startProjectActivity = latestProject
        .compose(Transformers.takeWhen(this.projectViewClicked))
        .map(p -> Pair.create(p, RefTag.dashboard()));
    }

    private final PublishSubject<Void> projectViewClicked = PublishSubject.create();

    private final Observable<String> projectNameTextViewText;
    private final Observable<Pair<Project, RefTag>> startProjectActivity;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override
    public void projectViewClicked() {
      this.projectViewClicked.onNext(null);
    }

    @Override
    public Observable<String> projectNameTextViewText() {
      return this.projectNameTextViewText;
    }

    @Override
    public Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
  }
}
