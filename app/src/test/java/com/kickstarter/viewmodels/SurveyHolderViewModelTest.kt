package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.SurveyResponseFactory.surveyResponse
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class SurveyHolderViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: SurveyHolderViewModel.ViewModel

    private val creatorAvatarImageUrl = TestSubscriber<String>()
    private val creatorNameTextViewText = TestSubscriber<String>()
    private val projectForSurveyDescription = TestSubscriber<Project>()
    private val startSurveyResponseActivity = TestSubscriber<SurveyResponse>()
    private val disposable = CompositeDisposable()

    private fun setUpEnvironment() {
        vm = SurveyHolderViewModel.ViewModel()
        vm.outputs.creatorAvatarImageUrl().subscribe { creatorAvatarImageUrl.onNext(it) }
            .addToDisposable(disposable)
        vm.outputs.creatorNameTextViewText().subscribe { creatorNameTextViewText.onNext(it) }
            .addToDisposable(disposable)
        vm.outputs.projectForSurveyDescription()
            .subscribe { projectForSurveyDescription.onNext(it) }.addToDisposable(disposable)
        vm.outputs.startSurveyResponseActivity()
            .subscribe { startSurveyResponseActivity.onNext(it) }.addToDisposable(disposable)
    }

    @Test
    fun testCreatorAvatarImageUrl() {
        val surveyResponse = surveyResponse()
        setUpEnvironment()

        vm.inputs.configureWith(surveyResponse)

        creatorAvatarImageUrl.assertValues(surveyResponse.project()?.creator()?.avatar()?.small())
    }

    @Test
    fun testCreatorNameEmits() {
        val surveyResponse = surveyResponse()
        setUpEnvironment()

        vm.inputs.configureWith(surveyResponse)

        creatorNameTextViewText.assertValues(surveyResponse.project()?.creator()?.name())
    }

    @Test
    fun testSurveyDescription() {
        val surveyResponse = surveyResponse()
        setUpEnvironment()

        vm.inputs.configureWith(surveyResponse)

        projectForSurveyDescription.assertValues(surveyResponse.project())
    }

    @Test
    fun testStartSurveyResponseActivity() {
        val surveyResponse = surveyResponse()

        setUpEnvironment()

        vm.inputs.configureWith(surveyResponse)
        vm.inputs.surveyClicked()

        startSurveyResponseActivity.assertValue(surveyResponse)
    }

    @After
    fun clear() {
        disposable.clear()
    }
}
