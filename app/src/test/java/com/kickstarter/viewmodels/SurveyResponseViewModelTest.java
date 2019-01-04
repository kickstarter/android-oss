package com.kickstarter.viewmodels;

import android.content.Intent;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.libs.Environment;
import com.kickstarter.mock.factories.SurveyResponseFactory;
import com.kickstarter.models.SurveyResponse;
import com.kickstarter.ui.IntentKey;

import org.junit.Test;

import androidx.annotation.NonNull;
import okhttp3.Request;
import rx.observers.TestSubscriber;

public class SurveyResponseViewModelTest extends KSRobolectricTestCase {
  private SurveyResponseViewModel.ViewModel vm;
  private final TestSubscriber<Void> goBack = new TestSubscriber<>();
  private final TestSubscriber<Void> showConfirmationDialog = new TestSubscriber<>();
  private final TestSubscriber<String> webViewUrl = new TestSubscriber<>();

  protected void setUpEnvironment(final @NonNull Environment environment) {
    this.vm = new SurveyResponseViewModel.ViewModel(environment);
    this.vm.outputs.goBack().subscribe(this.goBack);
    this.vm.outputs.showConfirmationDialog().subscribe(this.showConfirmationDialog);
    this.vm.outputs.webViewUrl().subscribe(this.webViewUrl);
  }

  @Test
  public void testGoBack() {
    setUpEnvironment(environment());
    this.vm.inputs.okButtonClicked();
    this.goBack.assertValueCount(1);
  }

  @Test
  public void testSubmitSuccessful_Redirect_ShowConfirmationDialog() {
    final String surveyUrl = "https://kck.str/projects/param/heyo/surveys/123";

    final SurveyResponse.Urls urlsEnvelope = SurveyResponse.Urls.builder()
      .web(SurveyResponse.Urls.Web.builder().survey(surveyUrl).build())
      .build();

    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse()
      .toBuilder()
      .urls(urlsEnvelope)
      .build();

    final Request projectSurveyRequest = new Request.Builder()
      .url(surveyUrl)
      .build();

    final Request projectRequest = new Request.Builder()
      .url("https://kck.str/projects/param/heyo")
      .tag(projectSurveyRequest)
      .build();

    setUpEnvironment(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse));

    // Survey loads. Successful submit redirects to project uri.
    this.vm.inputs.projectSurveyUriRequest(projectSurveyRequest);
    this.vm.inputs.projectUriRequest(projectRequest);

    // Success confirmation dialog is shown.
    this.showConfirmationDialog.assertValueCount(1);
  }

  @Test
  public void testWebViewUrl() {
    final SurveyResponse surveyResponse = SurveyResponseFactory.surveyResponse();

    setUpEnvironment(environment());
    this.vm.intent(new Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse));

    this.webViewUrl.assertValues(surveyResponse.urls().web().survey());
  }
}
