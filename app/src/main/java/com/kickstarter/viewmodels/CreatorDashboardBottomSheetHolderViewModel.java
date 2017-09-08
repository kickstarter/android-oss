package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;

import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardBottomSheetHolderViewModel {

  interface Inputs {
    void projectInput(Project project);
    void projectSwitcherProjectClicked();
  }
  interface Outputs {
    Observable<String> projectNameText();
    Observable<Project> projectSwitcherProject();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardBottomSheetViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectNameText = this.currentProject.map(Project::name);

      this.projectSwitcherProject = this.currentProject
        .compose(Transformers.takeWhen(this.projectSwitcherClicked));
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Project> currentProject = PublishSubject.create();
    private final PublishSubject<Void> projectSwitcherClicked = PublishSubject.create();

    private final Observable<String> projectNameText;
    private final Observable<Project> projectSwitcherProject;

    @Override
    public void projectInput(final @NonNull Project project) {
      this.currentProject.onNext(project);
    }

    @Override
    public void projectSwitcherProjectClicked() {
      this.projectSwitcherClicked.onNext(null);
    }

    @Override
    public @NonNull Observable<String> projectNameText() {
      return this.projectNameText;
    }
    @Override
    public @NonNull Observable<Project> projectSwitcherProject() {
      return this.projectSwitcherProject;
    }
  }
}
