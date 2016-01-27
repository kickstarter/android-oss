package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import rx.Observable;

public interface ThanksViewModelOutputs {
  Observable<Project> project();
}
