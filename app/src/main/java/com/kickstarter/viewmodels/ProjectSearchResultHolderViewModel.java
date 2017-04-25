package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.ProjectSearchResultViewHolder;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public interface ProjectSearchResultHolderViewModel {

  interface Inputs {
    /** Call to configure the view model with a message. */
    void configureWith(Project project);
  }

  interface Outputs {
    /** Emits key image of project */
    Observable<String> projectImage();

    /** Emits title of project */
    Observable<String> projectName();

    /** Emits a completed / days to go pair */
    Observable<Pair<Integer, Integer>> projectStats();
  }

  final class ViewModel extends ActivityViewModel<ProjectSearchResultViewHolder> implements
    ProjectSearchResultHolderViewModel.Inputs,
    ProjectSearchResultHolderViewModel.Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);
    }

    @Override public void configureWith(final @NonNull Project project) {
      projectImage.onNext(project.photo().med());
      projectName.onNext(project.name());
      projectStats.onNext(new Pair<>((int) project.percentageFunded(), ProjectUtils.deadlineCountdownValue(project)));
    }

    private final BehaviorSubject<String> projectImage = BehaviorSubject.create();
    private final BehaviorSubject<String> projectName = BehaviorSubject.create();
    private final BehaviorSubject<Pair<Integer, Integer>> projectStats = BehaviorSubject.create();

    public final ProjectSearchResultHolderViewModel.Inputs inputs = this;
    public final ProjectSearchResultHolderViewModel.Outputs outputs = this;

    @Override public Observable<String> projectImage() { return projectImage; }
    @Override public Observable<String> projectName() { return projectName; }
    @Override public Observable<Pair<Integer, Integer>> projectStats() { return projectStats; }
  }
}
