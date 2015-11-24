package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import java.util.List;

import rx.Observable;

public interface ProfilePresenterOutputs {
  Observable<List<Project>> projects();
  Observable<User> user();
}
