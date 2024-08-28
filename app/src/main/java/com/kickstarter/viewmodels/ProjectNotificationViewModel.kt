package com.kickstarter.viewmodels

import com.kickstarter.libs.Environment
import com.kickstarter.libs.rx.transformers.Transformers
import com.kickstarter.libs.rx.transformers.Transformers.errorsV2
import com.kickstarter.libs.rx.transformers.Transformers.valuesV2
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.libs.utils.extensions.isNotNull
import com.kickstarter.models.ProjectNotification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

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
        fun showUnableToSaveProjectNotificationError(): Observable<Unit>
    }

    class ViewModel(environment: Environment) : androidx.lifecycle.ViewModel(), Inputs, Outputs {

        private val enabledSwitchClick = PublishSubject.create<Boolean>()
        private val projectNotification = PublishSubject.create<ProjectNotification>()
        private val projectName = BehaviorSubject.create<String>()
        private val enabledSwitch = BehaviorSubject.create<Boolean>()
        private val showUnableToSaveProjectNotificationError = PublishSubject.create<Unit>()
        private val client = requireNotNull(environment.apiClientV2())
        private val disposables = CompositeDisposable()

        var inputs: Inputs = this
        var outputs: Outputs = this

        init {
            // When the enable switch is clicked, update the project notification.
            val updateNotification = projectNotification
                .compose(Transformers.takePairWhenV2(enabledSwitchClick))
                .switchMap {
                    client.updateProjectNotifications(it.first, it.second)
                }
                .materialize()
                .share()

            updateNotification
                .compose(valuesV2())
                .filter { it.isNotNull() }
                .subscribe { projectNotification.onNext(it) }
                .addToDisposable(disposables)

            updateNotification
                .compose(errorsV2())
                .subscribe { showUnableToSaveProjectNotificationError.onNext(Unit) }
                .addToDisposable(disposables)

            // Update the project name when a project notification emits.
            projectNotification
                .filter { it.project().isNotNull() && it.project().name().isNotNull() }
                .map { it.project().name() }
                .subscribe { projectName.onNext(it) }
                .addToDisposable(disposables)

            // Update the enabled switch when a project notification emits.
            projectNotification
                .map { it.email() && it.mobile() }
                .subscribe { enabledSwitch.onNext(it) }
                .addToDisposable(disposables)
        }

        override fun enabledSwitchClick(enabled: Boolean) {
            enabledSwitchClick.onNext(enabled)
        }

        override fun projectNotification(projectNotification: ProjectNotification) {
            this.projectNotification.onNext(projectNotification)
        }

        override fun projectName(): Observable<String> = projectName

        override fun enabledSwitch(): Observable<Boolean> = enabledSwitch

        override fun showUnableToSaveProjectNotificationError(): Observable<Unit> = showUnableToSaveProjectNotificationError

        override fun onCleared() {
            super.onCleared()
            disposables.clear()
        }
    }
}
