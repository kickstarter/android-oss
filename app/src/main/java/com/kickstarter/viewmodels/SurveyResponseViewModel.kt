package com.kickstarter.viewmodels

import android.util.Pair
import androidx.annotation.NonNull
import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import com.kickstarter.ui.activities.SurveyResponseActivity
import okhttp3.Request
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface SurveyResponseViewModel {
    interface Inputs {
        /** Call when the dialog's OK button has been clicked.  */
        fun okButtonClicked()

        /** Call when a project uri request has been made.  */
        fun projectUriRequest(request: Request)

        /** Call when a project survey uri request has been made.  */
        fun projectSurveyUriRequest(request: Request)
    }

    interface Outputs {
        /** Emits when we should navigate back.  */
        fun goBack(): Observable<Void>

        /** Emits when we should show a confirmation dialog.  */
        fun showConfirmationDialog(): Observable<Void>

        /** Emits a url to load in the web view.  */
        fun webViewUrl(): Observable<String?>
    }

    class ViewModel(@NonNull environment: Environment) :
        ActivityViewModel<SurveyResponseActivity>(environment), Inputs, Outputs {
        /**
         * Returns if a project request tag's url is a survey url,
         * which indicates a redirect from a successful submit.
         */
        private fun requestTagUrlIsSurveyUrl(projectRequestAndSurveyUrl: Pair<Request, String>): Boolean {
            val tag = projectRequestAndSurveyUrl.first.tag() as Request?
            return tag == null || tag.url.toString() == projectRequestAndSurveyUrl.second
        }

        private val okButtonClicked = PublishSubject.create<Void>()
        private val projectUriRequest = PublishSubject.create<Request>()
        private val projectSurveyUriRequest = PublishSubject.create<Request>()
        private val goBack: Observable<Void>
        private val showConfirmationDialog = PublishSubject.create<Void>()
        private val webViewUrl = BehaviorSubject.create<String?>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        init {
            val surveyResponse = intent()
                .map<Any?> { it.getParcelableExtra(IntentKey.SURVEY_RESPONSE) }
                .ofType(SurveyResponse::class.java)

            val surveyWebUrl = surveyResponse
                .map {
                    it.urls()?.web()?.survey()
                }

            surveyWebUrl
                .compose(bindToLifecycle())
                .subscribe(webViewUrl)

            val projectRequestAndSurveyUrl =
                Observable.combineLatest<Request, String?, Pair<Request, String>>(
                    projectUriRequest,
                    surveyWebUrl
                ) { a: Request?, b: String? -> Pair.create(a, b) }

            projectRequestAndSurveyUrl
                .filter { projectRequestAndSurveyUrl: Pair<Request, String> ->
                    requestTagUrlIsSurveyUrl(
                        projectRequestAndSurveyUrl
                    )
                }
                .compose(Transformers.ignoreValues())
                .compose(bindToLifecycle())
                .subscribe(showConfirmationDialog)

            goBack = okButtonClicked
        }

        override fun okButtonClicked() {
            okButtonClicked.onNext(null)
        }

        override fun projectUriRequest(request: Request) {
            projectUriRequest.onNext(request)
        }

        override fun projectSurveyUriRequest(request: Request) {
            projectSurveyUriRequest.onNext(request)
        }

        override fun goBack(): Observable<Void> = goBack

        override fun showConfirmationDialog(): Observable<Void> = showConfirmationDialog

        override fun webViewUrl(): Observable<String?> = webViewUrl
    }
}
