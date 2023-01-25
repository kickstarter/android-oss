package com.kickstarter.viewmodels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

interface ReportProjectViewModel {

    interface Inputs {
        fun createFlagging()
        fun inputDetails(s: String)
        fun kind(kind: String)
    }

    interface Outputs {
        fun projectUrl(): Observable<String>
        fun email(): Observable<String>
        fun finish(): Observable<Boolean>
        fun progressBarIsVisible(): Observable<Boolean>
    }

    class ReportProjectViewModel(
        private val environment: Environment,
        private val arguments: Bundle?
    ) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        private val apolloClient = requireNotNull(environment.apolloClientV2())

        private val userEmail = BehaviorSubject.create<String>()
        private val projectUrl = BehaviorSubject.create<String>()
        private val finish = PublishSubject.create<Boolean>()
        private val progressBarVisible = PublishSubject.create<Boolean>()

        private val sendButtonPressed = PublishSubject.create<Unit>()
        private val inputDetails = PublishSubject.create<String>()
        private val flaggingKind = PublishSubject.create<String>()

        private fun arguments() = Observable.just(this.arguments).filter { ObjectUtils.isNotNull(it) }.map { requireNotNull(it) }
        private val disposables = CompositeDisposable()

        init {

            val project = arguments()
                .map {
                    it.getParcelable(IntentKey.PROJECT) as Project?
                }
                .ofType(Project::class.java)

            disposables.add(
                project
                    .map {
                        it.urls().web().project()
                    }
                    .subscribe {
                        projectUrl.onNext(it)
                    }
            )

            disposables.add(
                apolloClient.userPrivacy()
                    .filter { ObjectUtils.isNotNull(it) }
                    .map { it.me()?.email() ?: "email@email.com" }
                    .subscribe {
                        userEmail.onNext(it)
                    }
            )

            val notification = sendButtonPressed
                .withLatestFrom(project) { _, project ->
                    return@withLatestFrom project
                }
                .withLatestFrom(inputDetails) { project, details ->
                    return@withLatestFrom Pair(project, details)
                }
                .withLatestFrom(flaggingKind) { pair, flaggingKind ->
                    return@withLatestFrom Triple(pair.first, pair.second, flaggingKind)
                }
                .switchMap {
                    this.apolloClient.createFlagging(it.first, it.second, it.third)
                        .doOnSubscribe {
                            this.progressBarVisible.onNext(true)
                        }
                        .doAfterTerminate {
                            this.progressBarVisible.onNext(false)
                        }
                        .materialize()
                }
                .share()

            disposables.add(
                notification
                    .compose(Transformers.valuesV2())
                    .subscribe {
                        finish.onNext(true)
                    }
            )
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        // - Outputs
        override fun email(): Observable<String> =
            this.userEmail

        override fun projectUrl(): Observable<String> =
            this.projectUrl

        override fun finish(): Observable<Boolean> =
            this.finish

        override fun progressBarIsVisible(): Observable<Boolean> =
            this.progressBarVisible

        // - Inputs
        override fun createFlagging() {
            sendButtonPressed.onNext(Unit)
        }

        override fun inputDetails(s: String) {
            inputDetails.onNext(s)
        }

        override fun kind(kind: String) {
            flaggingKind.onNext(kind)
        }
    }

    class Factory(private val environment: Environment, private val arguments: Bundle?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportProjectViewModel(environment, arguments) as T
        }
    }
}
