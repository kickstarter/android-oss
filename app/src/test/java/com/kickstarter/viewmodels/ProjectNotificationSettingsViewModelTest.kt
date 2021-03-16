package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.Environment
import com.kickstarter.mock.factories.ProjectNotificationFactory
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.ProjectNotification
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import java.util.*

class ProjectNotificationSettingsViewModelTest : KSRobolectricTestCase() {

    private lateinit var vm: ProjectNotificationSettingsViewModel.ViewModel

    private val projectNotifications = TestSubscriber<List<ProjectNotification>>()
    private val unableToFetchProjectNotificationsError = TestSubscriber<Void>()

    private fun setUpEnvironment(environment: Environment) {
        this.vm = ProjectNotificationSettingsViewModel.ViewModel(environment)

        this.vm.outputs.projectNotifications().subscribe(this.projectNotifications)
        this.vm.outputs.unableToFetchProjectNotificationsError().subscribe(this.unableToFetchProjectNotificationsError)
    }

    @Test
    fun testProjectNotifications() {
        val projectNotifications = Collections.singletonList(ProjectNotificationFactory.disabled())
        setUpEnvironment(
            environment().toBuilder().apiClient(object : MockApiClient() {
                override fun fetchProjectNotifications(): Observable<MutableList<ProjectNotification>> {
                    return Observable.just(projectNotifications)
                }
            }).build()
        )

        this.projectNotifications.assertValue(projectNotifications)
    }

    @Test
    fun testUnableToFetchProjectNotificationsError() {
        setUpEnvironment(
            environment().toBuilder().apiClient(object : MockApiClient() {
                override fun fetchProjectNotifications(): Observable<MutableList<ProjectNotification>> {
                    return Observable.error(Throwable("error"))
                }
            }).build()
        )

        this.unableToFetchProjectNotificationsError.assertValueCount(1)
    }
}
