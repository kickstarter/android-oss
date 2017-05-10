package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.viewholders.UnansweredSurveyViewHolder;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface UnansweredSurveyHolderViewModel {

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

    /** Emits the survey description */
    Observable<List<String>> surveyDescription();

    /** Emits the survey url */
    Observable<SurveyResponse> loadSurvey();
  }

  final class ViewModel extends ActivityViewModel<UnansweredSurveyViewHolder> implements
    UnansweredSurveyHolderViewModel.Inputs,
    UnansweredSurveyHolderViewModel.Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.creatorAvatarImage = this.configData
        .map(sr -> sr.project().creator().avatar().small());

      this.creatorName = this.configData
        .map(sr -> sr.project().creator().name());

      this.surveyDescription = this.configData
        .map(this::getSurveyDescription);

      this.goToSurvey = this.configData
        .compose(takeWhen(this.surveyClicked));
    }

    private final PublishSubject<SurveyResponse> configData = PublishSubject.create();
    private final PublishSubject<Void> surveyClicked = PublishSubject.create();

    private final Observable<String> creatorAvatarImage;
    private final Observable<String> creatorName;
    private final Observable<List<String>> surveyDescription;
    private final Observable<SurveyResponse> goToSurvey;

    public final UnansweredSurveyHolderViewModel.Inputs inputs = this;
    public final UnansweredSurveyHolderViewModel.Outputs outputs = this;

    @Override public void configureWith(final @NonNull SurveyResponse surveyResponse) {
      this.configData.onNext(surveyResponse);
    }
    @Override public void surveyClicked() {
      this.surveyClicked.onNext(null);
    }

    @Override public Observable<String> creatorAvatarImage() {
      return this.creatorAvatarImage;
    }
    @Override public Observable<String> creatorName() {
      return this.creatorName;
    }
    @Override public Observable<List<String>> surveyDescription() {
      return this.surveyDescription;
    }
    @Override public Observable<SurveyResponse> loadSurvey() {
      return this.configData;
    }

    private List<String> getSurveyDescription(final @NonNull SurveyResponse surveyResponse) {
      return Arrays.asList(surveyResponse.project().creator().name(), surveyResponse.project().name());
    }
  }

}
