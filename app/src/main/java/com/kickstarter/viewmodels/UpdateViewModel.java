package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.KoalaContext;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.UpdateActivity;
import com.kickstarter.ui.intentmappers.ProjectIntentMapper;

import androidx.annotation.NonNull;
import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.neverError;
import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface UpdateViewModel {

  interface Inputs {
    /** Call when an external link has been activated. */
    void externalLinkActivated();

    /** Call when a project update comments uri request has been made. */
    void goToCommentsRequest(Request request);

    /** Call when a project uri request has been made. */
    void goToProjectRequest(Request request);

    /** Call when a project update uri request has been made. */
    void goToUpdateRequest(Request request);

    /** Call when the share button is clicked. */
    void shareIconButtonClicked();
  }

  interface Outputs {
    /** Emits when we should start the share intent to show the share sheet. */
    Observable<Update> startShareIntent();

    /** Emits an update to start the comments activity with. */
    Observable<Update> startCommentsActivity();

    /** Emits a project and a ref tag to start the project activity with. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** Emits a string to display in the toolbar title. */
    Observable<String> updateSequence();

    /** Emits a url to load in the web view. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<UpdateActivity> implements Inputs, Outputs {
    private final ApiClientType client;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();

      final Observable<Update> initialUpdate = intent()
        .map(i -> i.getParcelableExtra(IntentKey.UPDATE))
        .ofType(Update.class)
        .take(1);

      final Observable<Project> project = intent()
        .flatMap(i -> ProjectIntentMapper.project(i, this.client))
        .share();

      final Observable<String> initialUpdateUrl = initialUpdate
        .map(u -> u.urls().web().update());

      final Observable<String> anotherUpdateUrl = this.goToUpdateRequest
        .map(request -> request.url().toString());

      Observable.merge(initialUpdateUrl, anotherUpdateUrl)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl::onNext);

      final Observable<Update> anotherUpdate = this.goToUpdateRequest
        .map(this::projectUpdateParams)
        .switchMap(pu -> this.client.fetchUpdate(pu.first, pu.second).compose(neverError()))
        .share();

      final Observable<Update> currentUpdate = Observable.merge(initialUpdate, anotherUpdate);

      currentUpdate
        .compose(takeWhen(this.shareButtonClicked))
        .compose(bindToLifecycle())
        .subscribe(this.startShareIntent::onNext);

      currentUpdate
        .compose(takeWhen(this.goToCommentsRequest))
        .compose(bindToLifecycle())
        .subscribe(this.startCommentsActivity::onNext);

      currentUpdate
        .map(u -> NumberUtils.format(u.sequence()))
        .compose(bindToLifecycle())
        .subscribe(this.updateSequence::onNext);

      project
        .compose(takeWhen(this.goToProjectRequest))
        .compose(bindToLifecycle())
        .subscribe(p -> this.startProjectActivity.onNext(Pair.create(p, RefTag.update())));

      project
        .compose(takeWhen(this.externalLinkActivated))
        .compose(bindToLifecycle())
        .subscribe(p -> this.koala.trackOpenedExternalLink(p, KoalaContext.ExternalLink.PROJECT_UPDATE));
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
    private final PublishSubject<Request> goToProjectRequest = PublishSubject.create();
    private final PublishSubject<Request> goToUpdateRequest = PublishSubject.create();
    private final PublishSubject<Void> shareButtonClicked = PublishSubject.create();

    private final PublishSubject<Update> startShareIntent = PublishSubject.create();
    private final PublishSubject<Update> startCommentsActivity = PublishSubject.create();
    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();
    private final BehaviorSubject<String> updateSequence = BehaviorSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void externalLinkActivated() {
      this.externalLinkActivated.onNext(null);
    }
    @Override public void goToCommentsRequest(final @NonNull Request request) {
      this.goToCommentsRequest.onNext(request);
    }
    @Override public void goToProjectRequest(final @NonNull Request request) {
      this.goToProjectRequest.onNext(request);
    }
    @Override public void goToUpdateRequest(final @NonNull Request request) {
      this.goToUpdateRequest.onNext(request);
    }
    @Override public void shareIconButtonClicked() {
      this.shareButtonClicked.onNext(null);
    }

    @Override public Observable<Update> startShareIntent() {
      return this.startShareIntent;
    }
    @Override public @NonNull Observable<Update> startCommentsActivity() {
      return this.startCommentsActivity;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> updateSequence() {
      return this.updateSequence;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return this.webViewUrl;
    }
  }
}
