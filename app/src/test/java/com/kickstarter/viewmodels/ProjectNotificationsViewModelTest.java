package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.mock.factories.ApiExceptionFactory;
import com.kickstarter.mock.factories.ProjectNotificationFactory;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.ProjectNotification;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.mock.services.MockApiClient;

import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;

public final class ProjectNotificationsViewModelTest extends KSRobolectricTestCase {
  private ProjectNotificationViewModel.ViewModel vm;
  private final TestSubscriber<Boolean> enabledSwitchTest = new TestSubscriber<>();
  private final TestSubscriber<String> projectNameTest = new TestSubscriber<>();
  private final TestSubscriber<Void> showUnableToSaveNotificationErrorTest = new TestSubscriber<>();

  @Test
  public void testNotificationsEmitProjectNameAndEnabledSwitch() {
    this.vm = new ProjectNotificationViewModel.ViewModel(environment());

    this.vm.outputs.projectName().subscribe(this.projectNameTest);
    this.vm.outputs.enabledSwitch().subscribe(this.enabledSwitchTest);

    // Start with an enabled notification.
    final ProjectNotification enabledNotification = ProjectNotificationFactory.enabled();
    this.vm.inputs.projectNotification(enabledNotification);

    // Project name and enabled values should match enabled notification.
    this.projectNameTest.assertValue(enabledNotification.project().name());
    this.enabledSwitchTest.assertValue(true);

    // Change to a disabled notification.
    final ProjectNotification disabledNotification = ProjectNotificationFactory.disabled();
    this.vm.inputs.projectNotification(disabledNotification);

    // Project name and enabled values should match disabled notification.
    this.projectNameTest.assertValues(enabledNotification.project().name(), disabledNotification.project().name());
    this.enabledSwitchTest.assertValues(true, false);
  }

  @Test
  public void testSwitchClickEmitsEnabledSwitch() {
    this.vm = new ProjectNotificationViewModel.ViewModel(environment());

    this.vm.outputs.enabledSwitch().subscribe(this.enabledSwitchTest);

    // Start with a disabled notification.
    final ProjectNotification disabledNotification = ProjectNotificationFactory.disabled();
    this.vm.inputs.projectNotification(disabledNotification);

    // Enabled switch should be disabled.
    this.enabledSwitchTest.assertValues(false);

    // Enable the previously disabled notification.
    this.vm.inputs.enabledSwitchClick(true);

    // Enabled switch should now be enabled.
    this.enabledSwitchTest.assertValues(false, true);
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

    this.vm = new ProjectNotificationViewModel.ViewModel(environment);

    this.vm.outputs.showUnableToSaveProjectNotificationError().subscribe(this.showUnableToSaveNotificationErrorTest);
    this.vm.outputs.enabledSwitch().subscribe(this.enabledSwitchTest);

    // Start with a disabled notification.
    final ProjectNotification projectNotification = ProjectNotificationFactory.disabled();
    this.vm.inputs.projectNotification(projectNotification);

    // Switch should be disabled.
    this.enabledSwitchTest.assertValue(false);

    // Attempt to toggle the notification to true. This should error, and the switch should still be disabled.
    this.vm.enabledSwitchClick(true);
    this.showUnableToSaveNotificationErrorTest.assertValueCount(1);
    this.enabledSwitchTest.assertValue(false);
  }
}
