package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.SurveyResponseFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.models.SurveyResponse;

import org.junit.Test;

import rx.observers.TestSubscriber;

public class SurveyHolderViewModelTest extends KSRobolectricTestCase {
  private SurveyHolderViewModel.ViewModel vm;
  private final TestSubscriber<String> creatorAvatarImage = new TestSubscriber<>();
  private final TestSubscriber<String> creatorName = new TestSubscriber<>();
  private final TestSubscriber<Project> projectForSurveyDescription = new TestSubscriber<>();
  private final TestSubscriber<SurveyResponse> startSurveyWebViewActivity = new TestSubscriber<>();

  private void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new SurveyHolderViewModel.ViewModel(environment);

    this.vm.outputs.creatorAvatarImageUrl().subscribe(this.creatorAvatarImage);
    this.vm.outputs.creatorNameTextViewText().subscribe(this.creatorName);
    this.vm.outputs.projectForSurveyDescription().subscribe(this.projectForSurveyDescription);
    this.vm.outputs.startSurveyWebViewActivity().subscribe(this.startSurveyWebViewActivity);
  }

  @Test
  public void testCreatorAvatarImageUrl() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorAvatarImage.assertValues(surveyResponse.project().creator().avatar().small());
  }

  @Test
  public void testCreatorNameEmits() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.creatorName.assertValues(surveyResponse.project().creator().name());
  }

  @Test
  public void testSurveyDescription() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.projectForSurveyDescription.assertValues(surveyResponse.project());
  }

  @Test
  public void testClickingSurveyEmitsUrl() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();
    setUpEnvironment(environment());
    this.vm.inputs.configureWith(surveyResponse);
    this.startSurveyWebViewActivity.assertValue(surveyResponse);
  }
}
