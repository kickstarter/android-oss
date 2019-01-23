package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectUpdatesActivity;

import androidx.annotation.NonNull;
import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takePairWhen;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface ProjectUpdatesViewModel {

  interface Inputs {
    /** Call when an external link has been activated. */
    void externalLinkActivated();

    /** Call when a project update comments uri request has been made. */
    void goToCommentsRequest(Request request);

    /** Call when a project update uri request has been made. */
    void goToUpdateRequest(Request request);

    /** Call when a project updates uri request has been made. */
    void goToUpdatesRequest(Request request);
  }

  interface Outputs {
    /** Emits an update to start the comments activity with. */
    Observable<Update> startCommentsActivity();

    /** Emits a project and an update to start the update activity with. */
    Observable<Pair<Project, Update>> startUpdateActivity();

    /** Emits a url to load in the web view. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<ProjectUpdatesActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final Observable<Project> project = intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .take(1);

      final Observable<String> initialUpdatesIndexUrl = project
        .map(Project::updatesUrl);

      final Observable<String> anotherIndexUrl = this.goToUpdatesRequest
        .map(request -> request.url().toString());

      Observable.merge(initialUpdatesIndexUrl, anotherIndexUrl)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl::onNext);

      this.goToCommentsRequest
        .map(this::projectUpdateParams)
        .switchMap(this::fetchUpdate)
        .compose(bindToLifecycle())
        .subscribe(this.startCommentsActivity::onNext);

      final Observable<Update> goToUpdateRequest = this.goToUpdateRequest
        .map(this::projectUpdateParams)
        .switchMap(this::fetchUpdate)
        .share();

      project
        .compose(takePairWhen(goToUpdateRequest))
        .compose(bindToLifecycle())
        .subscribe(this.startUpdateActivity::onNext);

      project
        .compose(takeWhen(this.externalLinkActivated))
        .compose(bindToLifecycle())
        .subscribe(p -> this.koala.trackOpenedExternalLink(p, KoalaContext.ExternalLink.PROJECT_UPDATES));

      project
        .compose(takeWhen(goToUpdateRequest))
        .compose(bindToLifecycle())
        .subscribe(p -> this.koala.trackViewedUpdate(p, KoalaContext.Update.UPDATES));

      project
        .take(1)
        .compose(bindToLifecycle())
        .subscribe(this.koala::trackViewedUpdates);
    }

    private @NonNull Observable<Update> fetchUpdate(final @NonNull Pair<String, String> projectAndUpdateParams) {
      return this.client
        .fetchUpdate(projectAndUpdateParams.first, projectAndUpdateParams.second)
        .compose(neverError());
    }

    /**
     * Parses a request for project and update params.
     *
     * @param request   Comments or update request.
     * @return          Pair of project param string and update param string.
     */
    private @NonNull Pair<String, String> projectUpdateParams(final @NonNull Request request) {
      // todo: build a Navigation helper for better param extraction
      final String projectParam = request.url().encodedPathSegments().get(2);
      final String updateParam = request.url().encodedPathSegments().get(4);
      return Pair.create(projectParam, updateParam);
    }

    private final PublishSubject<Request> externalLinkActivated = PublishSubject.create();
    private final PublishSubject<Request> goToCommentsRequest = PublishSubject.create();
    private final PublishSubject<Request> goToUpdateRequest = PublishSubject.create();
    private final PublishSubject<Request> goToUpdatesRequest = PublishSubject.create();

    private final PublishSubject<Update> startCommentsActivity = PublishSubject.create();
    private final PublishSubject<Pair<Project, Update>> startUpdateActivity = PublishSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void externalLinkActivated() {
      this.externalLinkActivated.onNext(null);
    }
    @Override public void goToCommentsRequest(final @NonNull Request request) {
      this.goToCommentsRequest.onNext(request);
    }
    @Override public void goToUpdateRequest(final @NonNull Request request) {
      this.goToUpdateRequest.onNext(request);
    }
    @Override
    public void goToUpdatesRequest(final @NonNull Request request) {
      this.goToUpdatesRequest.onNext(request);
    }

    @Override public @NonNull Observable<Update> startCommentsActivity() {
      return this.startCommentsActivity;
    }
    @Override public @NonNull Observable<Pair<Project, Update>> startUpdateActivity() {
      return this.startUpdateActivity;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return this.webViewUrl;
    }
  }
}
