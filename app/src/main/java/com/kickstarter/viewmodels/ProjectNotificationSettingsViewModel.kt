package com.kickstarter.viewmodels

import androidx.lifecycle.ViewModelProvider
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.ProjectNotification
import com.kickstarter.services.ApiClientTypeV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject

interface ProjectNotificationSettingsViewModel {
    interface Outputs {
        fun projectNotifications(): Observable<List<ProjectNotification>>
        fun unableToFetchProjectNotificationsError(): Observable<Unit>
    }

    class ProjectNotificationSettingsViewModel(environment: Environment) : androidx.lifecycle.ViewModel(), Outputs {
        private val client: ApiClientTypeV2 = requireNotNull(environment.apiClientV2())
        private val projectNotifications = BehaviorSubject.create<List<ProjectNotification>>()
        private val unableToFetchProjectNotificationsError = BehaviorSubject.create<Unit>()
        private val disposables = CompositeDisposable()

        val outputs: Outputs = this

        override fun projectNotifications(): Observable<List<ProjectNotification>> = projectNotifications

        override fun unableToFetchProjectNotificationsError(): Observable<Unit> = unableToFetchProjectNotificationsError

        init {
            val projectNotificationsNotification = client
                .fetchProjectNotifications()
                .materialize()

            projectNotificationsNotification
                .compose(Transformers.valuesV2())
                .filter { it.isNotNull() }
                .subscribe {
                    projectNotifications.onNext(it)
                }
                .addToDisposable(disposables)

            projectNotificationsNotification
                .compose(Transformers.errorsV2())
                .subscribe { unableToFetchProjectNotificationsError.onNext(Unit) }
                .addToDisposable(disposables)
        }

        override fun onCleared() {
            super.onCleared()
            disposables.clear()
        }
    }

    class Factory(private val environment: Environment) : ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return ProjectNotificationSettingsViewModel(environment) as T
        }
    }
}
