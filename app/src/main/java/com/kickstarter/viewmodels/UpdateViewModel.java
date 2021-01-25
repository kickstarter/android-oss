package com.kickstarter.viewmodels;

import android.net.Uri;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.Secrets;
import com.kickstarter.libs.utils.UrlUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.KSUri;
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
    /** Emits a project url to open externally. */
    Observable<String> openProjectExternally();

    /** Emits when we should start the share intent to show the share sheet. */
    Observable<Pair<Update, String>> startShareIntent();

    /** Emits an update to start the comments activity with. */
    Observable<Update> startCommentsActivity();

    /** Emits a Uri and a ref tag to start the project activity with. */
    Observable<Pair<Uri, RefTag>> startProjectActivity();

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
        .flatMap(i -> ProjectIntentMapper.project(i, this.client).compose(neverError()))
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
        .map(update -> Pair.create(update, UrlUtils.INSTANCE.appendRefTag(update.urls().web().update(), RefTag.updateShare().tag())))
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

      this.goToProjectRequest
        .map(request -> Uri.parse(request.url().uri().toString()))
        .filter(uri -> KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> !KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .compose(bindToLifecycle())
        .subscribe(uri -> this.startProjectActivity.onNext(Pair.create(uri, RefTag.update())));

      this.goToProjectRequest
        .map(request -> Uri.parse(request.url().uri().toString()))
        .filter(uri -> KSUri.isProjectUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .filter(uri -> KSUri.isProjectPreviewUri(uri, Secrets.WebEndpoint.PRODUCTION))
        .map(Uri::toString)
        .map(uriString -> UrlUtils.INSTANCE.refTag(uriString) == null
          ? UrlUtils.INSTANCE.appendRefTag(uriString, RefTag.update().tag())
          : uriString)
        .compose(bindToLifecycle())
        .subscribe(this.openProjectExternally::onNext);
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

    private final PublishSubject<String> openProjectExternally = PublishSubject.create();
    private final PublishSubject<Pair<Update, String>> startShareIntent = PublishSubject.create();
    private final PublishSubject<Update> startCommentsActivity = PublishSubject.create();
    private final PublishSubject<Pair<Uri, RefTag>> startProjectActivity = PublishSubject.create();
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

    @Override public @NonNull Observable<String> openProjectExternally() {
      return this.openProjectExternally;
    }
    @Override public Observable<Pair<Update, String>> startShareIntent() {
      return this.startShareIntent;
    }
    @Override public @NonNull Observable<Update> startCommentsActivity() {
      return this.startCommentsActivity;
    }
    @Override public @NonNull Observable<Pair<Uri, RefTag>> startProjectActivity() {
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
