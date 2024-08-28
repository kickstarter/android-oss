package com.kickstarter.viewmodels

import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.utils.extensions.addToDisposable
import com.kickstarter.mock.factories.ApiExceptionFactory
import com.kickstarter.mock.factories.ProjectNotificationFactory.disabled
import com.kickstarter.mock.factories.ProjectNotificationFactory.enabled
import com.kickstarter.mock.services.MockApiClientV2
import com.kickstarter.models.ProjectNotification
import com.kickstarter.services.ApiClientTypeV2
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subscribers.TestSubscriber
import org.junit.After
import org.junit.Test

class ProjectNotificationsViewModelTest : KSRobolectricTestCase() {
    private lateinit var vm: ProjectNotificationViewModel.ViewModel

    private val enabledSwitchTest = TestSubscriber<Boolean>()
    private val projectNameTest = TestSubscriber<String>()
    private val showUnableToSaveNotificationErrorTest = TestSubscriber<Unit>()
    private val disposables = CompositeDisposable()

    @Test
    fun testNotificationsEmitProjectNameAndEnabledSwitch() {
        vm = ProjectNotificationViewModel.ViewModel(environment())
        vm.outputs.projectName().subscribe { projectNameTest.onNext(it) }
            .addToDisposable(disposables)
        vm.outputs.enabledSwitch().subscribe { enabledSwitchTest.onNext(it) }
            .addToDisposable(disposables)

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
        val client: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun updateProjectNotifications(
                projectNotification: ProjectNotification,
                checked: Boolean
            ): Observable<ProjectNotification> {
                return Observable.just(projectNotification.toBuilder().email(checked).mobile(checked).build())
            }
        }
        vm = ProjectNotificationViewModel.ViewModel(environment().toBuilder().apiClientV2(client).build())
        vm.outputs.enabledSwitch().subscribe { enabledSwitchTest.onNext(it) }
            .addToDisposable(disposables)

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
        val client: ApiClientTypeV2 = object : MockApiClientV2() {
            override fun updateProjectNotifications(
                projectNotification: ProjectNotification,
                checked: Boolean
            ): Observable<ProjectNotification> {
                return Observable.error(ApiExceptionFactory.badRequestException())
            }
        }

        val environment = environment().toBuilder()
            .apiClientV2(client)
            .build()
        vm = ProjectNotificationViewModel.ViewModel(environment)

        vm.outputs.showUnableToSaveProjectNotificationError().subscribe {
            showUnableToSaveNotificationErrorTest.onNext(it)
        }.addToDisposable(disposables)
        vm.outputs.enabledSwitch().subscribe { enabledSwitchTest.onNext(it) }
            .addToDisposable(disposables)

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

    @After
    fun clear() {
        disposables.clear()
    }
}
