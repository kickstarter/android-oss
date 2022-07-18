package com.kickstarter.viewmodels

import com.kickstarter.libs.ActivityViewModel
import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.models.ProjectNotification
import com.kickstarter.ui.viewholders.ProjectNotificationViewHolder
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

interface ProjectNotificationViewModel {
    interface Inputs {
        /** Call when the enable switch is clicked.  */
        fun enabledSwitchClick(enabled: Boolean)

        /** Call when a notification is bound to the viewholder.  */
        fun projectNotification(projectNotification: ProjectNotification)
    }

    interface Outputs {
        /** Emits `True` if the enabled switch should be toggled on, `False` otherwise.  */
        fun enabledSwitch(): Observable<Boolean>

        /** Emits the project's name.  */
        fun projectName(): Observable<String>

        /**  Show an error indicating the notification cannot be saved.  */
        fun showUnableToSaveProjectNotificationError(): Observable<Void>
    }

    class ViewModel(environment: Environment) :
        ActivityViewModel<ProjectNotificationViewHolder?>(environment), Inputs, Outputs {

        private val enabledSwitchClick = PublishSubject.create<Boolean>()
        private val projectNotification = PublishSubject.create<ProjectNotification>()
        private val projectName = BehaviorSubject.create<String>()
        private val enabledSwitch = BehaviorSubject.create<Boolean>()
        private val showUnableToSaveProjectNotificationError = PublishSubject.create<Void>()


        var inputs: Inputs = this
        var outputs: Outputs = this


        init {
            val client = requireNotNull(environment.apiClient())

            // When the enable switch is clicked, update the project notification.
            val updateNotification = projectNotification
                .compose(Transformers.takePairWhen(enabledSwitchClick))
                .switchMap {
                    client
                        .updateProjectNotifications(it.first, it.second)
                        .materialize()
                }
                .share()

            updateNotification
                .compose(Transformers.values())
                .compose(bindToLifecycle())
                .subscribe {  projectNotification.onNext(it) }

            updateNotification
                .compose(Transformers.errors())
                .compose(bindToLifecycle())
                .subscribe { showUnableToSaveProjectNotificationError.onNext(null) }

            // Update the project name when a project notification emits.
            projectNotification
                .map { it.project().name() }
                .compose(bindToLifecycle())
                .subscribe {projectName.onNext(it) }

            // Update the enabled switch when a project notification emits.
            projectNotification
                .map { it.email() && it.mobile() }
                .compose(bindToLifecycle())
                .subscribe {enabledSwitch.onNext(it) }
        }

        override fun enabledSwitchClick(enabled: Boolean) {
            enabledSwitchClick.onNext(enabled)
        }

        override fun projectNotification(projectNotification: ProjectNotification) {
            this.projectNotification.onNext(projectNotification)
        }

        override fun projectName(): Observable<String> = projectName

        override fun enabledSwitch(): Observable<Boolean> = enabledSwitch


        override fun showUnableToSaveProjectNotificationError(): Observable<Void> =showUnableToSaveProjectNotificationError
    }
}