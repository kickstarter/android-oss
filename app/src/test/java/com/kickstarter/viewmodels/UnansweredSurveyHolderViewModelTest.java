package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.SurveyResponseFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class UnansweredSurveyHolderViewModelTest extends KSRobolectricTestCase {

  private UnansweredSurveyHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> creatorAvatarImage = new TestSubscriber<>();
  private final TestSubscriber<String> creatorName = new TestSubscriber<>();
  private final TestSubscriber<SurveyResponse> loadSurvey = new TestSubscriber<>();
  private final TestSubscriber<Project> projectForSurveyDescription = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new UnansweredSurveyHolderViewModel.ViewModel(environment);

    this.vm.outputs.creatorAvatarImage().subscribe(this.creatorAvatarImage);
    this.vm.outputs.creatorName().subscribe(this.creatorName);
    this.vm.outputs.loadSurvey().subscribe(this.loadSurvey);
    this.vm.outputs.projectForSurveyDescription().subscribe(this.projectForSurveyDescription);
  }

  @Test
  public void creatorAvatarImage() throws Exception {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorAvatarImage.assertValues(surveyResponse.project().creator().avatar().small());
  }
  @Test
  public void creatorName() throws Exception {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorName.assertValues(surveyResponse.project().creator().name());
  }
  @Test
  public void surveyDescription() throws Exception {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.projectForSurveyDescription.assertValues(surveyResponse.project());
  }
  @Test
  public void clickingSurveyEmitsUrl() throws Exception {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.loadSurvey.assertValue(surveyResponse);
  }
}
