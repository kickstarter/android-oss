package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.ProjectNotification
import com.kickstarter.services.ApiClientType
import com.kickstarter.ui.activities.ProjectNotificationSettingsActivity
import rx.Observable
import rx.subjects.BehaviorSubject

interface ProjectNotificationSettingsViewModel {
    interface Outputs {
        fun projectNotifications(): Observable<List<ProjectNotification>>
        fun unableToFetchProjectNotificationsError(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ProjectNotificationSettingsActivity>(environment), Outputs {
        private val client: ApiClientType
        private val projectNotifications = BehaviorSubject.create<List<ProjectNotification>>()
        private val unableToFetchProjectNotificationsError = BehaviorSubject.create<Void>()

        val outputs: Outputs = this

        override fun projectNotifications(): Observable<List<ProjectNotification>> = projectNotifications

        override fun unableToFetchProjectNotificationsError(): Observable<Void> = unableToFetchProjectNotificationsError

        init {
            client = requireNotNull(environment.apiClient())

            val projectNotificationsNotification = client
                .fetchProjectNotifications()
                .materialize()

            projectNotificationsNotification
                .compose(Transformers.values())
                .compose(bindToLifecycle())
                .subscribe {
                    projectNotifications.onNext(it)
                }

            projectNotificationsNotification
                .compose(Transformers.errors())
                .compose(bindToLifecycle())
                .subscribe { unableToFetchProjectNotificationsError.onNext(null) }
        }
    }
}
