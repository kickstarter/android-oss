package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardBottomSheetHolderViewModel {

  interface Inputs {
    void projectInput(Project project);
  }
  interface Outputs {
    Observable<String> projectName();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardBottomSheetViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectName = this.currentProject
      .map(Project::name);
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Project> currentProject = PublishSubject.create();

    private final Observable<String> projectName;

    @Override
    public void projectInput(Project project) {
      this.currentProject.onNext(project);
    }

    @Override
    public @NonNull Observable<String> projectName() { return this.projectName; }
  }
}
