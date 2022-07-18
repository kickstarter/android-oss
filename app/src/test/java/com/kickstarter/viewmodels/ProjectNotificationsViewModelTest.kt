package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ProjectNotificationFactory.disabled
import com.kickstarter.mock.factories.ProjectNotificationFactory.enabled
import com.kickstarter.mock.services.MockApiClient
import com.kickstarter.models.ProjectNotification
import com.kickstarter.services.ApiClientType
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber

class ProjectNotificationsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectNotificationViewModel.ViewModel
    
    private val enabledSwitchTest = TestSubscriber<Boolean>()
    private val projectNameTest = TestSubscriber<String>()
    private val showUnableToSaveNotificationErrorTest = TestSubscriber<Void>()
    
    @Test
    fun testNotificationsEmitProjectNameAndEnabledSwitch() {
        vm = ProjectNotificationViewModel.ViewModel(environment())
        vm.outputs.projectName().subscribe(projectNameTest)
        vm.outputs.enabledSwitch().subscribe(enabledSwitchTest)

        // Start with an enabled notification.
        val enabledNotification = enabled()
        vm.inputs.projectNotification(enabledNotification)

        // Project name and enabled values should match enabled notification.
        projectNameTest.assertValue(enabledNotification.project().name())
        enabledSwitchTest.assertValue(true)

        // Change to a disabled notification.
        val disabledNotification = disabled()
        vm.inputs.projectNotification(disabledNotification)

        // Project name and enabled values should match disabled notification.
        projectNameTest.assertValues(
            enabledNotification.project().name(),
            disabledNotification.project().name()
        )
        enabledSwitchTest.assertValues(true, false)
    }

    @Test
    fun testSwitchClickEmitsEnabledSwitch() {
        vm = ProjectNotificationViewModel.ViewModel(environment())
        vm.outputs.enabledSwitch().subscribe(enabledSwitchTest)

        // Start with a disabled notification.
        val disabledNotification = disabled()
        vm.inputs.projectNotification(disabledNotification)

        // Enabled switch should be disabled.
        enabledSwitchTest.assertValues(false)

        // Enable the previously disabled notification.
        vm.inputs.enabledSwitchClick(true)

        // Enabled switch should now be enabled.
        enabledSwitchTest.assertValues(false, true)
    }

    @Test
    fun testShowUnableToSaveNotificationError() {
        val client: ApiClientType = object : MockApiClient() {
            override fun updateProjectNotifications(
                projectNotification: ProjectNotification,
                checked: Boolean
            ): Observable<ProjectNotification> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder()
            .apiClient(client)
            .build()
        vm = ProjectNotificationViewModel.ViewModel(environment)

        vm.outputs.showUnableToSaveProjectNotificationError().subscribe(
            showUnableToSaveNotificationErrorTest
        )
        vm.outputs.enabledSwitch().subscribe(enabledSwitchTest)

        // Start with a disabled notification.
        val projectNotification = disabled()
        vm.inputs.projectNotification(projectNotification)

        // Switch should be disabled.
        enabledSwitchTest.assertValue(false)

        // Attempt to toggle the notification to true. This should error, and the switch should still be disabled.
        vm.enabledSwitchClick(true)
        showUnableToSaveNotificationErrorTest.assertValueCount(1)
        enabledSwitchTest.assertValue(false)
    }
}