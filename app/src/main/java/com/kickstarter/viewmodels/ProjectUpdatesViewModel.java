package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectUpdatesActivity;
import com.kickstarter.viewmodels.inputs.ProjectUpdatesViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProjectUpdatesViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class ProjectUpdatesViewModel extends ActivityViewModel<ProjectUpdatesActivity> implements ProjectUpdatesViewModelInputs,
  ProjectUpdatesViewModelOutputs {
  private final ApiClientType client;

  public ProjectUpdatesViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();

    final Observable<Project> initialProject = intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .filter(ObjectUtils::isNotNull);

    // todo: more url logic
    final Observable<String> initialIndexUrl = initialProject.map(Project::updatesUrl);

    initialIndexUrl
      .compose(bindToLifecycle())
      .subscribe(this.webViewUrl::onNext);

    startCommentsActivity = PublishSubject.create();
  }

  private final PublishSubject<String> pageInterceptedUrlSubject = PublishSubject.create();

  private final Observable<Update> startCommentsActivity;
  private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

  public final ProjectUpdatesViewModelInputs inputs = this;
  public final ProjectUpdatesViewModelOutputs outputs = this;

  @Override public void pageInterceptedUrl(final @NonNull String url) {
    this.pageInterceptedUrlSubject.onNext(url);
  }

  @Override public @NonNull Observable<Update> startCommentsActivity() {
    return startCommentsActivity;
  }
  @Override public @NonNull Observable<String> webViewUrl() {
    return webViewUrl;
  }
}
