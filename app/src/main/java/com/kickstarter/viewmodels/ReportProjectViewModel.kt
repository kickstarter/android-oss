package com.kickstarter.viewmodels

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.ObjectUtils
import com.kickstarter.models.Project
import com.kickstarter.ui.IntentKey
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject


//abstract class BaseReportProjectViewModel(val environment: Environment, val arguments: Bundle?): ViewModel() {
//    abstract fun projectUrl(): Observable<String>
//}
interface ReportProjectViewModel {

    interface Inputs {
    }

    interface Outputs {
        fun projectUrl(): PublishSubject<String>
        fun email(): PublishSubject<String>
    }

    class ReportProjectViewModel(
        private val environment: Environment,
        private val arguments: Bundle?) : ViewModel(), Inputs, Outputs {
        val inputs: Inputs = this
        val outputs: Outputs = this

        val apolloClient = requireNotNull(environment.apolloClient())
        val currentUser = requireNotNull(environment.currentUser()?.observable())
        private val projectInput = BehaviorSubject.create<Project>()
        private val userEmail = PublishSubject.create<String>()
        private val projectUrl = PublishSubject.create<String>()

        private fun arguments() = Observable.just(this.arguments).filter { ObjectUtils.isNotNull(it) }.map { requireNotNull(it) }
        private val disposables = CompositeDisposable()

        init {
            disposables.add(arguments()
                .map {
                    it.getParcelable(IntentKey.PROJECT) as Project?
                }
                .ofType(Project::class.java)
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
        }

        override fun onCleared() {
            disposables.clear()
            super.onCleared()
        }

        override fun email(): PublishSubject<String> = this.userEmail
        override fun projectUrl(): PublishSubject<String> = this.projectUrl

    }

    class Factory(private val environment: Environment, private val arguments: Bundle?) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportProjectViewModel(environment, arguments) as T
        }
    }
}
