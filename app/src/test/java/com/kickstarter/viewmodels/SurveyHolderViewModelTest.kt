package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import org.junit.Test
import rx.observers.TestSubscriber

class SurveyHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SurveyHolderViewModel.ViewModel

    private val creatorAvatarImageUrl = TestSubscriber<String>()
    private val creatorNameTextViewText = TestSubscriber<String>()
    private val projectForSurveyDescription = TestSubscriber<Project?>()
    private val startSurveyResponseActivity = TestSubscriber<SurveyResponse>()

    private fun setUpEnvironment(environment: Environment) {
        vm = SurveyHolderViewModel.ViewModel(environment)
        vm.outputs.creatorAvatarImageUrl().subscribe(creatorAvatarImageUrl)
        vm.outputs.creatorNameTextViewText().subscribe(creatorNameTextViewText)
        vm.outputs.projectForSurveyDescription().subscribe(projectForSurveyDescription)
        vm.outputs.startSurveyResponseActivity().subscribe(startSurveyResponseActivity)
    }

    @Test
    fun testCreatorAvatarImageUrl() {
        val surveyResponse = surveyResponse()
        setUpEnvironment(environment())

        vm.inputs.configureWith(surveyResponse)

        creatorAvatarImageUrl.assertValues(surveyResponse.project()?.creator()?.avatar()?.small())
    }

    @Test
    fun testCreatorNameEmits() {
        val surveyResponse = surveyResponse()
        setUpEnvironment(environment())

        vm.inputs.configureWith(surveyResponse)

        creatorNameTextViewText.assertValues(surveyResponse.project()?.creator()?.name())
    }

    @Test
    fun testSurveyDescription() {
        val surveyResponse = surveyResponse()
        setUpEnvironment(environment())

        vm.inputs.configureWith(surveyResponse)

        projectForSurveyDescription.assertValues(surveyResponse.project())
    }

    @Test
    fun testStartSurveyResponseActivity() {
        val surveyResponse = surveyResponse()

        setUpEnvironment(environment())

        vm.inputs.configureWith(surveyResponse)
        vm.inputs.surveyClicked()

        startSurveyResponseActivity.assertValue(surveyResponse)
    }
}
