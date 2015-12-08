package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import java.util.List;

import rx.Observable;

public interface ManageProjectNotificationsOutputs {
  Observable<List<Project>> projects();
}
