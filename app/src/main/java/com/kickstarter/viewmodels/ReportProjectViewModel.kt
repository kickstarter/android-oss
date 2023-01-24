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
    }

    interface Outputs {
        fun projectUrl(): Observable<String>
        fun email(): Observable<String>
    }

    class ReportProjectViewModel(
        private val environment: Environment,
        private val arguments: Bundle?) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val apolloClient = requireNotNull(environment.apolloClientV2())
        val currentUser = requireNotNull(environment.currentUser()?.observable())
        private val userEmail = BehaviorSubject.create<String>()
        private val projectUrl = BehaviorSubject.create<String>()

        private val sendButtonPressed = PublishSubject.create<Unit>()

        private fun arguments() = Observable.just(this.arguments).filter { ObjectUtils.isNotNull(it) }.map { requireNotNull(it) }
        private val disposables = CompositeDisposable()

        init {

            val project = arguments()
                .map {
                    it.getParcelable(IntentKey.PROJECT) as Project?
                }
                .ofType(Project::class.java)

            disposables.add(project
                .map {
                    it.urls().web().project()
                }
                .subscribe {
                    projectUrl.onNext(it)
                })

            currentUser
                .filter { ObjectUtils.isNotNull(it) }
                .map { it.email() ?: "email@email.com" }
                .subscribe {
                    userEmail.onNext(it)
                }

            val notification = sendButtonPressed
                .map {
                    it
                }
                .withLatestFrom(project){_, project ->
                    return@withLatestFrom project
                }
                .switchMap {
                    this.apolloClient.createFlagging(it)
                        .doOnSubscribe {
                            userEmail.onNext("second@email.com")
                        }
                        .doAfterTerminate {
                            userEmail.onNext("third@email.com")
                        }
                        .materialize()
                }
                .share()

            disposables.add(
                notification
                    .compose(Transformers.valuesV2())
                    .map {
                        it
                    }
                    .subscribe {
                        this.userEmail.onNext("fourth@gmail.com")
                    }
            )
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun email(): Observable<String> =
            this.userEmail
        override fun projectUrl(): Observable<String> =
            this.projectUrl

        override fun createFlagging() {
            sendButtonPressed.onNext(Unit)
        }

    }

    class Factory(private val environment: Environment, private val arguments: Bundle?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportProjectViewModel(environment, arguments) as T
        }
    }
}
