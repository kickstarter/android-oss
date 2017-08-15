package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.RefTag;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.SurveyResponseActivity;

import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface SurveyResponseViewModel {

  interface Inputs {
    /** Call when a project uri request has been made. */
    void goToProjectRequest(Request request);

    void pojectSurveyUriRequest(Request request);

    /** Call when the dialog's OK button has been clicked. */
    void okButtonClicked();
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
    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<SurveyResponse> surveyResponse = intent()
        .map(i -> i.getParcelableExtra(IntentKey.SURVEY_RESPONSE))
        .ofType(SurveyResponse.class);

      surveyResponse
        .map(s -> s.urls().web().toString())
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl);

      this.goBack = this.okButtonClicked;

      // todo: start project activity via fetching params from request

      // todo: show dialog when should redirect
//      let redirectAfterPostRequest = self.shouldStartLoadProperty.signal.skipNil()
//        .filter { request, navigationType in
//        isUnpreparedSurvey(request: request) && navigationType == .other
//      }
//      .map { request, _ in request }

      Observable<Request> redirectAfterPostRequest = this.projectSurveyUriRequest
        .filter(this::isUnpreparedSurvey);
    }

    // todo: filter out unprepared survey requests
//    public func isPrepared(request: URLRequest) -> Bool {
//      return request.value(forHTTPHeaderField: "Authorization") == authorizationHeader
//        && request.value(forHTTPHeaderField: "Kickstarter-iOS-App") != nil
//    }
    private boolean isUnpreparedSurvey(final @NonNull Request request) {

      return false;
    }

    private final PublishSubject<Request> goToProjectRequest = PublishSubject.create();
    private final PublishSubject<Request> projectSurveyUriRequest = PublishSubject.create();
    private final PublishSubject<Void> okButtonClicked = PublishSubject.create();

    private final Observable<Void> goBack;
    private final PublishSubject<Void> showConfirmationDialog = PublishSubject.create();
    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void goToProjectRequest(final @NonNull Request request) {
      this.goToProjectRequest.onNext(request);
    }
    @Override public void pojectSurveyUriRequest(final @NonNull Request request) {
      this.projectSurveyUriRequest.onNext(request);
    }
    @Override public void okButtonClicked() {
      this.okButtonClicked.onNext(null);
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
