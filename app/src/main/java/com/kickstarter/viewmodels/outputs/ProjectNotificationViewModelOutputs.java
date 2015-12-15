package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Notification;

import rx.Observable;

public interface ProjectNotificationViewModelOutputs {
  Observable<Notification> notification();
  Observable<Void> updateSuccess();
}
