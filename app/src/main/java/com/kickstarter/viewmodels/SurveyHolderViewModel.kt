package com.kickstarter.viewmodels

import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.Project
import com.kickstarter.models.SurveyResponse
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface SurveyHolderViewModel {
    interface Inputs {
        /** Call to configure the view model with a survey  */
        fun configureWith(surveyResponse: SurveyResponse)

        /** Call when card is clicked.  */
        fun surveyClicked()
    }

    interface Outputs {
        /** Emits creator avatar image  */
        fun creatorAvatarImageUrl(): Observable<String>

        /** Emits the creator name  */
        fun creatorNameTextViewText(): Observable<String>

        /** Emits the project from survey  */
        fun projectForSurveyDescription(): Observable<Project>

        /** Emits when we should start the [com.kickstarter.ui.activities.SurveyResponseActivity].  */
        fun startSurveyResponseActivity(): Observable<SurveyResponse>
    }

    class ViewModel : Inputs, Outputs {
        private val surveyResponse = PublishSubject.create<SurveyResponse>()
        private val surveyClicked = PublishSubject.create<Unit>()
        private val creatorAvatarImageUrl: Observable<String>
        private val creatorNameTextViewText: Observable<String>
        private val projectForSurveyDescription: Observable<Project>
        private val startSurveyResponseActivity: Observable<SurveyResponse>

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            creatorAvatarImageUrl = surveyResponse
                .map { it.project() }
                .filter { it.isNotNull() }
                .map { it.creator().avatar().small() }

            creatorNameTextViewText = surveyResponse
                .map { it.project() }
                .filter { it.isNotNull() }
                .map { it.creator().name() }

            projectForSurveyDescription = surveyResponse
                .map { it.project() }

            startSurveyResponseActivity = surveyResponse
                .compose(Transformers.takeWhenV2(surveyClicked))
        }

        override fun configureWith(surveyResponse: SurveyResponse) {
            this.surveyResponse.onNext(surveyResponse)
        }

        override fun surveyClicked() {
            surveyClicked.onNext(Unit)
        }

        override fun creatorAvatarImageUrl(): Observable<String> = creatorAvatarImageUrl

        override fun creatorNameTextViewText(): Observable<String> = creatorNameTextViewText

        override fun projectForSurveyDescription(): Observable<Project> =
            projectForSurveyDescription

        override fun startSurveyResponseActivity(): Observable<SurveyResponse> =
            startSurveyResponseActivity
    }
}
