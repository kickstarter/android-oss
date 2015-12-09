package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import java.util.List;

import rx.Observable;

public interface SearchViewModelOutputs {
  Observable<List<Project>> searchProjects();
  Observable<List<Project>> popularProjects();
}
