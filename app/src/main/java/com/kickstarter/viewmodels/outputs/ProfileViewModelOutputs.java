package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import java.util.List;

import rx.Observable;

public interface ProfileViewModelOutputs {
  Observable<List<Project>> projects();
  Observable<User> user();
  Observable<Project> showProject();
  Observable<Void> showDiscovery();
}
