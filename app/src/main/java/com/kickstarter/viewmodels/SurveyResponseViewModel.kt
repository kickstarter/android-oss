package com.kickstarter.viewmodels

import android.content.Intent
import android.util.Pair
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.models.SurveyResponse
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.Request

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
        fun goBack(): Observable<Unit>

        /** Emits when we should show a confirmation dialog.  */
        fun showConfirmationDialog(): Observable<Unit>

        /** Emits a url to load in the web view.  */
        fun webViewUrl(): Observable<String>
    }

    class ViewModel(environment: Environment, val intent: Intent) :
        androidx.lifecycle.ViewModel(), Inputs, Outputs {
        /**
         * Returns if a project request tag's url is a survey url,
         * which indicates a redirect from a successful submit.
         */
        private fun requestTagUrlIsSurveyUrl(projectRequestAndSurveyUrl: Pair<Request, String>): Boolean {
            val tag = projectRequestAndSurveyUrl.first.tag() as Request?
            return tag == null || tag.url.toString() == projectRequestAndSurveyUrl.second
        }

        private val okButtonClicked = PublishSubject.create<Unit>()
        private val projectUriRequest = PublishSubject.create<Request>()
        private val projectSurveyUriRequest = PublishSubject.create<Request>()
        private val goBack: Observable<Unit>
        private val showConfirmationDialog = PublishSubject.create<Unit>()
        private val webViewUrl = BehaviorSubject.create<String>()

        val inputs: Inputs = this
        val outputs: Outputs = this

        private fun intent() = intent.let { Observable.just(it) }

        private val disposables = CompositeDisposable()

        init {
            val surveyResponse = intent()
                .map<Any?> { it.getParcelableExtra(IntentKey.SURVEY_RESPONSE) }
                .ofType(SurveyResponse::class.java)

            val surveyWebUrl = surveyResponse
                .map {
                    it.urls()?.web()?.survey() ?: ""
                }

            surveyWebUrl
                .subscribe {
                    webViewUrl.onNext(it)
                }
                .addToDisposable(disposables)

            val projectRequestAndSurveyUrl =
                Observable.combineLatest(
                    projectUriRequest,
                    surveyWebUrl
                ) { a: Request, b: String -> Pair.create(a, b) }

            projectRequestAndSurveyUrl
                .filter { pUriRequestSurveyUrl: Pair<Request, String> ->
                    requestTagUrlIsSurveyUrl(
                        pUriRequestSurveyUrl
                    )
                }
                .compose(Transformers.ignoreValuesV2())
                .subscribe(showConfirmationDialog)

            goBack = okButtonClicked
        }

        override fun okButtonClicked() {
            okButtonClicked.onNext(Unit)
        }

        override fun projectUriRequest(request: Request) {
            projectUriRequest.onNext(request)
        }

        override fun projectSurveyUriRequest(request: Request) {
            projectSurveyUriRequest.onNext(request)
        }

        override fun goBack(): Observable<Unit> = goBack

        override fun showConfirmationDialog(): Observable<Unit> = showConfirmationDialog

        override fun webViewUrl(): Observable<String> = webViewUrl
    }

    class Factory(private val environment: Environment, private val intent: Intent) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ViewModel(environment, intent) as T
        }
    }
}
