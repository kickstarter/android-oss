package com.kickstarter.viewmodels;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.SurveyResponseFactory;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;

import org.junit.Test;

import androidx.annotation.NonNull;
import rx.observers.TestSubscriber;

public class SurveyHolderViewModelTest extends KSRobolectricTestCase {
  private SurveyHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> creatorAvatarImageUrl = new TestSubscriber<>();
  private final TestSubscriber<String> creatorNameTextViewText = new TestSubscriber<>();
  private final TestSubscriber<Project> projectForSurveyDescription = new TestSubscriber<>();
  private final TestSubscriber<SurveyResponse> startSurveyResponseActivity = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new SurveyHolderViewModel.ViewModel(environment);

    this.vm.outputs.creatorAvatarImageUrl().subscribe(this.creatorAvatarImageUrl);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorNameTextViewText);
    this.vm.outputs.projectForSurveyDescription().subscribe(this.projectForSurveyDescription);
    this.vm.outputs.startSurveyResponseActivity().subscribe(this.startSurveyResponseActivity);
  }

  @Test
  public void testCreatorAvatarImageUrl() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorAvatarImageUrl.assertValues(surveyResponse.project().creator().avatar().small());
  }

  @Test
  public void testCreatorNameEmits() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorNameTextViewText.assertValues(surveyResponse.project().creator().name());
  }

  @Test
  public void testSurveyDescription() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.projectForSurveyDescription.assertValues(surveyResponse.project());
  }

  @Test
  public void testStartSurveyResponseActivity() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());

    this.vm.inputs.configureWith(surveyResponse);
    this.vm.inputs.surveyClicked();

    this.startSurveyResponseActivity.assertValue(surveyResponse);
  }
}
