package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.factories.ApiExceptionFactory;
import com.kickstarter.factories.ProjectNotificationFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.MockApiClient;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public final class ProjectNotificationViewModelTest extends KSRobolectricTestCase {
  @Test
  public void testNotificationsEmitProjectNameAndEnabledSwitch() {
    final ProjectNotificationViewModel vm = new ProjectNotificationViewModel(environment());

    final TestSubscriber<String> projectNameTest = new TestSubscriber<>();
    vm.outputs.projectName().subscribe(projectNameTest);

    final TestSubscriber<Boolean> enabledSwitchTest = new TestSubscriber<>();
    vm.outputs.enabledSwitch().subscribe(enabledSwitchTest);

    // Start with an enabled notification.
    final ProjectNotification enabledNotification = ProjectNotificationFactory.enabled();
    vm.inputs.projectNotification(enabledNotification);

    // Project name and enabled values should match enabled notification.
    projectNameTest.assertValue(enabledNotification.project().name());
    enabledSwitchTest.assertValue(true);

    // Change to a disabled notification.
    final ProjectNotification disabledNotification = ProjectNotificationFactory.disabled();
    vm.inputs.projectNotification(disabledNotification);

    // Project name and enabled values should match disabled notification.
    projectNameTest.assertValues(enabledNotification.project().name(), disabledNotification.project().name());
    enabledSwitchTest.assertValues(true, false);
  }

  @Test
  public void testSwitchClickEmitsEnabledSwitch() {
    final ProjectNotificationViewModel vm = new ProjectNotificationViewModel(environment());

    final TestSubscriber<Boolean> enabledSwitchTest = new TestSubscriber<>();
    vm.outputs.enabledSwitch().subscribe(enabledSwitchTest);

    // Start with a disabled notification.
    final ProjectNotification disabledNotification = ProjectNotificationFactory.disabled();
    vm.inputs.projectNotification(disabledNotification);

    // Enabled switch should be disabled.
    enabledSwitchTest.assertValues(false);

    // Enable the previously disabled notification.
    vm.inputs.enabledSwitchClick(true);

    // Enabled switch should now be enabled.
    enabledSwitchTest.assertValues(false, true);
  }

  @Test
  public void testShowUnableToSaveNotificationError() {
    final ApiClientType client = new MockApiClient() {
      @Override
      public @NonNull Observable<ProjectNotification> updateProjectNotifications(final @NonNull ProjectNotification projectNotification, final boolean checked) {
        return Observable.error(ApiExceptionFactory.badRequestException());
      }
    };

    final Environment environment = environment().toBuilder()
      .apiClient(client)
      .build();

    final ProjectNotificationViewModel vm = new ProjectNotificationViewModel(environment);

    final TestSubscriber<Void> showUnableToSaveNotificationErrorTest = new TestSubscriber<>();
    vm.errors.showUnableToSaveProjectNotificationError().subscribe(showUnableToSaveNotificationErrorTest);

    final TestSubscriber<Boolean> enabledSwitchTest = new TestSubscriber<>();
    vm.outputs.enabledSwitch().subscribe(enabledSwitchTest);

    // Start with a disabled notification.
    final ProjectNotification projectNotification = ProjectNotificationFactory.disabled();
    vm.inputs.projectNotification(projectNotification);

    // Switch should be disabled.
    enabledSwitchTest.assertValue(false);

    // Attempt to toggle the notification to true. This should error, and the switch should still be disabled.
    vm.enabledSwitchClick(true);
    showUnableToSaveNotificationErrorTest.assertValueCount(1);
    enabledSwitchTest.assertValue(false);
  }
}
