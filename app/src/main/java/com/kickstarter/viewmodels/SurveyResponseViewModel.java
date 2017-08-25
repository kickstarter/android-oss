package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.SurveyResponseActivity;

import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;
import static com.kickstarter.libs.rx.transformers.Transformers.neverError;

public interface SurveyResponseViewModel {

  interface Inputs {
    /** Call when a project uri request has been made. */
    void projectUriRequest(Request request);

    /** Call when the dialog's OK button has been clicked. */
    void okButtonClicked();

    /** Call when a project survey uri request has been made. */
    void projectSurveyUriRequest(Request request);

    /** Call when a web view page has been intercepted. */
    void webViewPageIntercepted(String url);
  }

  interface Outputs {
    /** Emits when we should navigate back. */
    Observable<Void> goBack();

    /** Emits when we should show a confirmation dialog. */
    Observable<Void> showConfirmationDialog();

    /** Emits a project and a ref tag to start the {@link com.kickstarter.ui.activities.ProjectActivity} with. */
    Observable<Pair<Project, RefTag>> startProjectActivity();

    /** Emits a url to load in the web view. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<SurveyResponseActivity> implements Inputs, Outputs {
    private final ApiClientType client;
    private final CurrentUserType currentUser;

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.client = environment.apiClient();
      this.currentUser = environment.currentUser();

      final Observable<SurveyResponse> surveyResponse = intent()
        .map(i -> i.getParcelableExtra(IntentKey.SURVEY_RESPONSE))
        .ofType(SurveyResponse.class);

      final Observable<Request> initialRequest = surveyResponse
        .map(s -> s.urls().web().survey())
        .map(url -> new Request.Builder().url(url).build());

      final Observable<Request> shouldLoadSurveyRequest = this.projectSurveyUriRequest
        .filter(r -> isPrepared(r, this.currentUser))
        .distinctUntilChanged();

      final Observable<Request> postRequest = shouldLoadSurveyRequest
        .filter(request -> "POST".equals(request.method()))
        .filter(r ->
          isUnpreparedSurvey(r, this.currentUser)
        ); // && navigationType == .formSubmitted

      // todo: the problem is the redirect is a survey request w a diff nav type
      final Observable<Request> redirectAfterPostRequest = this.projectSurveyUriRequest
        .filter(r ->
          isUnpreparedSurvey(r, this.currentUser)
        );  // && navigationType == .other

      final Observable<Request> surveyRequest = Observable.merge(
        initialRequest,
        postRequest
      );

      surveyRequest
        .map(r -> r.url().toString())
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl);

      this.showConfirmationDialog = redirectAfterPostRequest
        .compose(ignoreValues());

      final Observable<Project> project = this.projectUriRequest
        .map(this::extractProjectParams)
        .switchMap(this.client::fetchProject)
        .compose(neverError())
        .share();

      project
        .compose(bindToLifecycle())
        .subscribe(p -> this.startProjectActivity.onNext(Pair.create(p, RefTag.survey())));

      this.goBack = this.okButtonClicked;
    }

    /**
     * Parses a project request for project params.
     */
    private @NonNull String extractProjectParams(final @NonNull Request request) {
      return request.url().encodedPathSegments().get(2);
    }

    private boolean isUnpreparedSurvey(final @NonNull Request surveyRequest, final @NonNull CurrentUserType currentUser) {
      return !isPrepared(surveyRequest, currentUser);
    }

    private boolean isPrepared(final @NonNull Request request, final @NonNull CurrentUserType currentUser) {
      final boolean isAuthorized = ("token " + currentUser.getAccessToken()).equals(request.header("Authorization"));
      final boolean isFromApp = request.header("Kickstarter-Android-App") != null;
      return isAuthorized && isFromApp;
    }

    private final PublishSubject<Request> projectUriRequest = PublishSubject.create();
    private final PublishSubject<Void> okButtonClicked = PublishSubject.create();
    private final PublishSubject<Request> projectSurveyUriRequest = PublishSubject.create();
    private final PublishSubject<String> webViewPageIntercepted = PublishSubject.create();

    private final Observable<Void> goBack;
    private final Observable<Void> showConfirmationDialog;
    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void projectUriRequest(final @NonNull Request request) {
      this.projectUriRequest.onNext(request);
    }
    @Override public void okButtonClicked() {
      this.okButtonClicked.onNext(null);
    }
    @Override public void projectSurveyUriRequest(final @NonNull Request request) {
      this.projectSurveyUriRequest.onNext(request);
    }
    @Override public void webViewPageIntercepted(final @NonNull String url) {
      this.webViewPageIntercepted.onNext(url);
    }

    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<Void> showConfirmationDialog() {
      return this.showConfirmationDialog;
    }
    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return this.webViewUrl;
    }
  }
}
