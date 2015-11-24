package com.kickstarter.presenters.outputs;

import com.kickstarter.models.Project;

import java.util.List;

import rx.Observable;

public interface ProfilePresenterOutputs {
  Observable<List<Project>> projects();
}
