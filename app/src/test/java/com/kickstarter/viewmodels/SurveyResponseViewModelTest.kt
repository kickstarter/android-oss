package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import okhttp3.Request
import org.junit.Test
import rx.observers.TestSubscriber

class SurveyResponseViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: SurveyResponseViewModel.ViewModel
    private val goBack = TestSubscriber<Void>()
    private val showConfirmationDialog = TestSubscriber<Void>()
    private val webViewUrl = TestSubscriber<String?>()

    protected fun setUpEnvironment(environment: Environment) {
        vm = SurveyResponseViewModel.ViewModel(environment)

        vm.outputs.goBack().subscribe(goBack)
        vm.outputs.showConfirmationDialog().subscribe(showConfirmationDialog)
        vm.outputs.webViewUrl().subscribe(webViewUrl)
    }

    @Test
    fun testGoBack() {
        setUpEnvironment(environment())
        vm.inputs.okButtonClicked()
        goBack.assertValueCount(1)
    }

    @Test
    fun testSubmitSuccessful_Redirect_ShowConfirmationDialog() {
        val surveyUrl = "https://kck.str/projects/param/heyo/surveys/123"

        val urlsEnvelope = SurveyResponse.Urls.builder()
            .web(SurveyResponse.Urls.Web.builder().survey(surveyUrl).build())
            .build()

        val surveyResponse = surveyResponse()
            .toBuilder()
            .urls(urlsEnvelope)
            .build()

        val projectSurveyRequest: Request = Request.Builder()
            .url(surveyUrl)
            .build()

        val projectRequest: Request = Request.Builder()
            .url("https://kck.str/projects/param/heyo")
            .tag(projectSurveyRequest)
            .build()

        setUpEnvironment(environment())
        vm.intent(Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

        // Survey loads. Successful submit redirects to project uri.
        vm.inputs.projectSurveyUriRequest(projectSurveyRequest)
        vm.inputs.projectUriRequest(projectRequest)

        // Success confirmation dialog is shown.
        showConfirmationDialog.assertValueCount(1)
    }

    @Test
    fun testSubmitSuccessful_NullTag_ShowConfirmationDialog() {
        val surveyUrl = "https://kck.str/projects/param/heyo/surveys/123"

        val urlsEnvelope = SurveyResponse.Urls.builder()
            .web(SurveyResponse.Urls.Web.builder().survey(surveyUrl).build())
            .build()

        val surveyResponse = surveyResponse()
            .toBuilder()
            .urls(urlsEnvelope)
            .build()

        val projectSurveyRequest: Request = Request.Builder()
            .url(surveyUrl)
            .build()

        val projectRequest: Request = Request.Builder()
            .url("https://kck.str/projects/param/heyo")
            .build()

        setUpEnvironment(environment())
        vm.intent(Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

        // Survey loads. Successful submit redirects to project uri.
        vm.inputs.projectSurveyUriRequest(projectSurveyRequest)
        vm.inputs.projectUriRequest(projectRequest)

        // Success confirmation dialog is shown.
        showConfirmationDialog.assertValueCount(1)
    }

    @Test
    fun testWebViewUrl() {
        val surveyResponse = surveyResponse()

        setUpEnvironment(environment())
        vm.intent(Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

        webViewUrl.assertValues(surveyResponse.urls()?.web()?.survey())
    }
}
