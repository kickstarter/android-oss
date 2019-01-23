package com.kickstarter.viewmodels;

import android.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.SurveyResponseActivity;

import androidx.annotation.NonNull;
import okhttp3.Request;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.ignoreValues;

public interface SurveyResponseViewModel {

  interface Inputs {
    /** Call when the dialog's OK button has been clicked. */
    void okButtonClicked();

    /** Call when a project uri request has been made. */
    void projectUriRequest(Request request);

    /** Call when a project survey uri request has been made. */
    void projectSurveyUriRequest(Request request);
  }

  interface Outputs {
    /** Emits when we should navigate back. */
    Observable<Void> goBack();

    /** Emits when we should show a confirmation dialog. */
    Observable<Void> showConfirmationDialog();

    /** Emits a url to load in the web view. */
    Observable<String> webViewUrl();
  }

  final class ViewModel extends ActivityViewModel<SurveyResponseActivity> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      final Observable<SurveyResponse> surveyResponse = intent()
        .map(i -> i.getParcelableExtra(IntentKey.SURVEY_RESPONSE))
        .ofType(SurveyResponse.class);

      final Observable<String> surveyWebUrl = surveyResponse
        .map(r -> r.urls().web().survey());

      surveyWebUrl
        .compose(bindToLifecycle())
        .subscribe(this.webViewUrl);

      final Observable<Pair<Request, String>> projectRequestAndSurveyUrl = Observable.combineLatest(
        this.projectUriRequest,
        surveyWebUrl,
        Pair::create
      );

      projectRequestAndSurveyUrl
        .filter(this::requestTagUrlIsSurveyUrl)
        .compose(ignoreValues())
        .compose(bindToLifecycle())
        .subscribe(this.showConfirmationDialog);

      this.goBack = this.okButtonClicked;
    }

    /**
     * Returns if a project request tag's url is a survey url,
     * which indicates a redirect from a successful submit.
     */
    private boolean requestTagUrlIsSurveyUrl(final @NonNull Pair<Request, String> projectRequestAndSurveyUrl) {
      return ((Request) projectRequestAndSurveyUrl.first.tag()).url().toString()
        .equals(projectRequestAndSurveyUrl.second);
    }

    private final PublishSubject<Void> okButtonClicked = PublishSubject.create();
    private final PublishSubject<Request> projectUriRequest = PublishSubject.create();
    private final PublishSubject<Request> projectSurveyUriRequest = PublishSubject.create();

    private final Observable<Void> goBack;
    private final PublishSubject<Void> showConfirmationDialog = PublishSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void okButtonClicked() {
      this.okButtonClicked.onNext(null);
    }
    @Override public void projectUriRequest(final @NonNull Request request) {
      this.projectUriRequest.onNext(request);
    }
    @Override public void projectSurveyUriRequest(final @NonNull Request request) {
      this.projectSurveyUriRequest.onNext(request);
    }

    @Override public @NonNull Observable<Void> goBack() {
      return this.goBack;
    }
    @Override public @NonNull Observable<Void> showConfirmationDialog() {
      return this.showConfirmationDialog;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return this.webViewUrl;
    }
  }
}
