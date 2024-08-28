package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ProjectNotificationFactory
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.ProjectNotification
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test
import java.util.Collections

class ProjectNotificationSettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectNotificationSettingsViewModel.ProjectNotificationSettingsViewModel

    private val projectNotifications = TestSubscriber<List<ProjectNotification>>()
    private val unableToFetchProjectNotificationsError = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    private fun setUpEnvironment(environment: Environment) {
        this.vm =
            ProjectNotificationSettingsViewModel.ProjectNotificationSettingsViewModel(environment)

        this.vm.outputs.projectNotifications().subscribe { this.projectNotifications.onNext(it) }
            .addToDisposable(disposables)
        this.vm.outputs.unableToFetchProjectNotificationsError()
            .subscribe { this.unableToFetchProjectNotificationsError.onNext(it) }
            .addToDisposable(disposables)
    }

    @Test
    fun testProjectNotifications() {
        val projectNotifications = Collections.singletonList(ProjectNotificationFactory.disabled())
        setUpEnvironment(
            environment().toBuilder().apiClientV2(object : MockApiClientV2() {
                override fun fetchProjectNotifications(): Observable<List<ProjectNotification>> {
                    return Observable.just(projectNotifications)
                }
            }).build()
        )

        this.projectNotifications.assertValue(projectNotifications)
    }

    @Test
    fun testUnableToFetchProjectNotificationsError() {
        setUpEnvironment(
            environment().toBuilder().apiClientV2(object : MockApiClientV2() {
                override fun fetchProjectNotifications(): Observable<List<ProjectNotification>> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

        this.unableToFetchProjectNotificationsError.assertValueCount(1)
    }

    @After
    fun clear() {
        disposables.clear()
    }
}
