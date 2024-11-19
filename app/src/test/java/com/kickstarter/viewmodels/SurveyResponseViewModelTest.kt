package com.kickstarter.viewmodels

import android.content.Intent
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import okhttp3.Request
import org.junit.After
import org.junit.Test

class SurveyResponseViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: SurveyResponseViewModel.ViewModel
    private val goBack = TestSubscriber<Unit>()
    private val showConfirmationDialog = TestSubscriber<Unit>()
    private val webViewUrl = TestSubscriber<String>()

    private val disposables = CompositeDisposable()

    protected fun setUpEnvironment(environment: Environment, intent: Intent) {
        vm = SurveyResponseViewModel.ViewModel(environment, intent)

        vm.outputs.goBack().subscribe { goBack.onNext(it) }.addToDisposable(disposables)
        vm.outputs.showConfirmationDialog().subscribe { showConfirmationDialog.onNext(it) }.addToDisposable(disposables)
        vm.outputs.webViewUrl().subscribe { webViewUrl.onNext(it) }.addToDisposable(disposables)
    }

    @Test
    fun testGoBack() {
        setUpEnvironment(environment(), Intent())
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

        setUpEnvironment(environment(), Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

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

        setUpEnvironment(environment(), Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

        // Survey loads. Successful submit redirects to project uri.
        vm.inputs.projectSurveyUriRequest(projectSurveyRequest)
        vm.inputs.projectUriRequest(projectRequest)

        // Success confirmation dialog is shown.
        showConfirmationDialog.assertValueCount(1)
    }

    @Test
    fun `open webview when survey is opened from activity feed`() {
        val surveyResponse = surveyResponse()

        setUpEnvironment(environment(), Intent().putExtra(IntentKey.SURVEY_RESPONSE, surveyResponse))

        webViewUrl.assertValues(surveyResponse.urls()?.web()?.survey())
    }

    @Test
    fun `open webview when survey is opened from notification and url contains host`() {
        val surveyUrlPath = "projects/1231313/test-project-notification/backing/survey_responses"

        setUpEnvironment(environment().toBuilder().webEndpoint("www.test.dev/").build(), Intent().putExtra(IntentKey.NOTIFICATION_SURVEY_RESPONSE, surveyUrlPath))

        webViewUrl.assertValues("www.test.dev/projects/1231313/test-project-notification/backing/survey_responses")
    }

    @Test
    fun `open webview when survey is opened from deeplink`() {
        val surveyUrlPath = "http://www.test.com/projects/1231313/test-project-deeplink/backing/survey_responses"

        setUpEnvironment(environment().toBuilder().webEndpoint("www.kickstarter.com").build(), Intent().putExtra(IntentKey.DEEPLINK_SURVEY_RESPONSE, surveyUrlPath))

        webViewUrl.assertValues("www.kickstarter.com/projects/1231313/test-project-deeplink/backing/survey_responses")
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
