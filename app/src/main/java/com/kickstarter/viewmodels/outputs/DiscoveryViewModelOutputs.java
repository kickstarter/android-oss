package com.kickstarter.viewmodels.outputs;

import com.kickstarter.models.Project;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  Observable<List<Project>> projects();
  Observable<Boolean> shouldShowOnboarding();
}
