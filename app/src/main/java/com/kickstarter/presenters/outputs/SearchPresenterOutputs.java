package com.kickstarter.presenters.outputs;

import android.util.Pair;

import com.kickstarter.models.Empty;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import rx.Observable;

public interface SearchPresenterOutputs {
  Observable<Empty> clear();
  Observable<Project> startProjectActivity();
  Observable<Pair<DiscoveryParams, List<Project>>> newData();
}
