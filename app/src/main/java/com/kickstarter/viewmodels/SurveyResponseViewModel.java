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

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public interface SurveyResponseViewModel {

  interface Inputs {
    void closeButtonClicked();
    void okButtonClicked();
  }

  interface Outputs {
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
    }

    private final PublishSubject<Void> closeButtonClicked = PublishSubject.create();
    private final PublishSubject<Void> okButtonClicked = PublishSubject.create();

    private final PublishSubject<Pair<Project, RefTag>> startProjectActivity = PublishSubject.create();
    private final BehaviorSubject<String> webViewUrl = BehaviorSubject.create();

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void closeButtonClicked() {
      this.closeButtonClicked.onNext(null);
    }
    @Override public void okButtonClicked() {
      this.okButtonClicked.onNext(null);
    }

    @Override public @NonNull Observable<Pair<Project, RefTag>> startProjectActivity() {
      return this.startProjectActivity;
    }
    @Override public @NonNull Observable<String> webViewUrl() {
      return this.webViewUrl;
    }
  }
}
