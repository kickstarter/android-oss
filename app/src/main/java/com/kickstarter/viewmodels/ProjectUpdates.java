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

import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static rx.Observable.combineLatest;
import static rx.Observable.merge;
import static rx.Observable.zip;

public interface ProjectUpdates {
  interface Inputs {
    /** Call when the web view page url has been intercepted. */
    void pageInterceptedUrl(String url);

    /** Call when a project update comments uri request has been made. */
    void goToCommentsRequest(Request request);

    /** Call when a project update uri request has been made. */
    void goToUpdateRequest(Request request);
  }

  interface Outputs {
    /** Emits an update to start the comments activity with. */
    Observable<Update> startCommentsActivity();

    /** Emits a project and an update to start the update activity with. */
    Observable<Pair<Project, Update>> startUpdateActivity();

    /** Emits a web view url to display. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<ProjectUpdatesActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    ViewModel(final @NonNull Environment environment) {
      super(environment);

      client = environment.apiClient();

      final Observable<Project> initialProject = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .filter(ObjectUtils::isNotNull);

      initialProject.map(Project::updatesUrl)
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl::onNext);

      final Observable<String> updateParam = merge(
        goToCommentsRequestSubject,
        goToUpdateRequestSubject
      )
        .map(this::updateParamFromRequest);

      final Observable<Update> updateFromParams = combineLatest(
        initialProject.map(Project::param),
        updateParam,
        Pair::create
      )
        .switchMap(pu ->
          client
            .fetchUpdate(pu.first, pu.second)
            .compose(Transformers.neverError())
        );

      updateFromParams
        .compose(bindToLifecycle())
        .subscribe(this.startCommentsActivity::onNext);

      zip(initialProject, updateFromParams, Pair::create)
        .compose(bindToLifecycle())
        .subscribe(this.startUpdateActivity::onNext);

      initialProject
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(__ -> koala.trackViewedUpdates());
    }

    private @NonNull String updateParamFromRequest(final @NonNull Request request) {
      // todo: build a safer param matcher helper--give group names to segments
      return request.url().encodedPathSegments().get(4);
    }

    private final PublishSubject<String> pageInterceptedUrlSubject = PublishSubject.create();
    private final PublishSubject<Request> goToCommentsRequestSubject = PublishSubject.create();
    private final PublishSubject<Request> goToUpdateRequestSubject = PublishSubject.create();

    private final BehaviorSubject<Update> startCommentsActivity = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Project, Update>> startUpdateActivity = BehaviorSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void pageInterceptedUrl(final @NonNull String url) {
      this.pageInterceptedUrlSubject.onNext(url);
    }
    @Override public void goToCommentsRequest(final @NonNull Request request) {
      this.goToCommentsRequestSubject.onNext(request);
    }
    @Override public void goToUpdateRequest(final @NonNull Request request) {
      this.goToUpdateRequestSubject.onNext(request);
    }
    @Override public @NonNull Observable<Update> startCommentsActivity() {
      return startCommentsActivity;
    }
    @Override public @NonNull Observable<Pair<Project, Update>> startUpdateActivity() {
      return startUpdateActivity;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return webViewUrl;
    }
  }
}
