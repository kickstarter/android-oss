package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.viewholders.SurveyViewHolder;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.kickstarter.libs.rx.transformers.Transformers.takeWhen;

public interface SurveyHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a survey */
    void configureWith(SurveyResponse surveyResponse);

    /** Call when card is clicked. */
    void surveyClicked();
  }

  interface Outputs {
    /** Emits creator avatar image */
    Observable<String> creatorAvatarImageUrl();

    /** Emits the creator name */
    Observable<String> creatorNameTextViewText();

    /** Emits the project from survey */
    Observable<Project> projectForSurveyDescription();

    /** Emits when we should start the {@link com.kickstarter.ui.activities.SurveyResponseActivity}. */
    Observable<SurveyResponse> startSurveyResponseActivity();
  }

  final class ViewModel extends ActivityViewModel<SurveyViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.creatorAvatarImageUrl = this.surveyResponse
        .map(SurveyResponse::project)
        .map(p -> p.creator().avatar().small());

      this.creatorNameTextViewText = this.surveyResponse
        .map(SurveyResponse::project)
        .map(p -> p.creator().name());

      this.projectForSurveyDescription = this.surveyResponse
        .map(SurveyResponse::project);

      this.startSurveyResponseActivity = this.surveyResponse
        .compose(takeWhen(this.surveyClicked));
    }

    private final PublishSubject<SurveyResponse> surveyResponse = PublishSubject.create();
    private final PublishSubject<Void> surveyClicked = PublishSubject.create();

    private final Observable<String> creatorAvatarImageUrl;
    private final Observable<String> creatorNameTextViewText;
    private final Observable<Project> projectForSurveyDescription;
    private final Observable<SurveyResponse> startSurveyResponseActivity;

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    @Override public void configureWith(final @NonNull SurveyResponse surveyResponse) {
      this.surveyResponse.onNext(surveyResponse);
    }
    @Override public void surveyClicked() {
      this.surveyClicked.onNext(null);
    }

    @Override public @NonNull Observable<String> creatorAvatarImageUrl() {
      return this.creatorAvatarImageUrl;
    }
    @Override public @NonNull Observable<String> creatorNameTextViewText() {
      return this.creatorNameTextViewText;
    }
    @Override public @NonNull Observable<Project> projectForSurveyDescription() {
      return this.projectForSurveyDescription;
    }
    @Override public @NonNull Observable<SurveyResponse> startSurveyResponseActivity() {
      return this.startSurveyResponseActivity;
    }
  }
}
