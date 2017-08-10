package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.viewholders.SurveyViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface SurveyHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a survey */
    void configureWith(SurveyResponse surveyResponse);

    /** Call when card is surveyClicked */
    void surveyClicked();
  }

  interface Outputs {
    /** Emits creator avatar image */
    Observable<String> creatorAvatarImage();

    /** Emits the creator name */
    Observable<String> creatorName();

    /** Emits the survey url */
    Observable<SurveyResponse> loadSurvey();

    /** Emits the project from survey */
    Observable<Project> projectForSurveyDescription();
  }

  final class ViewModel extends ActivityViewModel<SurveyViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.creatorAvatarImage = this.configData
        .map(sr -> sr.project().creator().avatar().small());

      this.creatorName = this.configData
        .map(sr -> sr.project().creator().name());

      this.projectForSurveyDescription = this.configData
        .map(SurveyResponse::project);

      this.goToSurvey = this.configData
        .compose(takeWhen(this.surveyClicked));
    }

    private final PublishSubject<SurveyResponse> configData = PublishSubject.create();
    private final PublishSubject<Void> surveyClicked = PublishSubject.create();

    private final Observable<String> creatorAvatarImage;
    private final Observable<String> creatorName;
    private final Observable<Project> projectForSurveyDescription;
    private final Observable<SurveyResponse> goToSurvey;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull SurveyResponse surveyResponse) {
      this.configData.onNext(surveyResponse);
    }
    @Override public void surveyClicked() {
      this.surveyClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> creatorAvatarImage() {
      return this.creatorAvatarImage;
    }
    @Override public @NonNull Observable<String> creatorName() {
      return this.creatorName;
    }
    @Override public @NonNull Observable<SurveyResponse> loadSurvey() {
      return this.configData;
    }
    @Override public @NonNull Observable<Project> projectForSurveyDescription() {
      return this.projectForSurveyDescription;
    }
  }
}
