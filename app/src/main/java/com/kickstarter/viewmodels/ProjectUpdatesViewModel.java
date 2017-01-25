package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectUpdatesActivity;
import com.kickstarter.viewmodels.inputs.ProjectUpdatesViewModelInputs;
import com.kickstarter.viewmodels.outputs.ProjectUpdatesViewModelOutputs;

import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static rx.Observable.combineLatest;

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

    // todo: add external url logic
    final Observable<String> initialIndexUrl = initialProject.map(Project::updatesUrl);

    initialIndexUrl
      .compose(bindToLifecycle())
      .subscribe(this.webViewUrl::onNext);

    final Observable<String> updateParam = updateCommentsRequestSubject
      .map(this::updateParamFromRequest);

    combineLatest(
      initialProject.map(Project::param),
      updateParam,
      Pair::create
    )
      .switchMap(pu ->
        client
          .fetchUpdate(pu.first, pu.second)
          .compose(Transformers.neverError())
      )
      .subscribe(this.startCommentsActivity::onNext);
  }

  private final String updateParamFromRequest(final @NonNull Request request) {
    // todo: build a safer param matcher helper--give group names to segments
    return request.url().encodedPathSegments().get(4);
  }

  private final PublishSubject<String> pageInterceptedUrlSubject = PublishSubject.create();
  private final PublishSubject<Request> updateCommentsRequestSubject = PublishSubject.create();

  private final BehaviorSubject<Update> startCommentsActivity = BehaviorSubject.create();
  private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

  public final ProjectUpdatesViewModelInputs inputs = this;
  public final ProjectUpdatesViewModelOutputs outputs = this;

  @Override public void pageInterceptedUrl(final @NonNull String url) {
    this.pageInterceptedUrlSubject.onNext(url);
  }
  @Override public void updateCommentsRequest(final @NonNull Request request) {
    this.updateCommentsRequestSubject.onNext(request);
  }

  @Override public @NonNull Observable<Update> startCommentsActivity() {
    return startCommentsActivity;
  }
  @Override public @NonNull Observable<String> webViewUrl() {
    return webViewUrl;
  }
}
