package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.ProjectNotification;

import java.util.List;

import rx.Observable;

public interface ProjectNotificationSettingsViewModelOutputs {
  Observable<List<ProjectNotification>> projectNotifications();
}
