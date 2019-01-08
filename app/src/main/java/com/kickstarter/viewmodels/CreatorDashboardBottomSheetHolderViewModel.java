package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Project;
import com.kickstarter.ui.viewholders.CreatorDashboardBottomSheetViewHolder;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.PublishSubject;

public interface CreatorDashboardBottomSheetHolderViewModel {

  interface Inputs {
    /** Current project. */
    void projectInput(Project project);

    /** Call when project is selected. */
    void projectSwitcherProjectClicked();
  }
  interface Outputs {
    /** Emits the project launch date to be formatted for display. */
    Observable<DateTime> projectLaunchDate();

    /** Emits the project name for display. */
    Observable<String> projectNameText();

    /** Emits when project is selected. */
    Observable<Project> projectSwitcherProject();
  }

  final class ViewModel extends ActivityViewModel<CreatorDashboardBottomSheetViewHolder> implements Inputs, Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      this.projectNameText = this.currentProject
        .map(Project::name);

      this.projectLaunchDate = this.currentProject
        .map(Project::launchedAt);

      this.projectSwitcherProject = this.currentProject
        .compose(Transformers.takeWhen(this.projectSwitcherClicked));
    }

    public final Inputs inputs = this;
    public final Outputs outputs = this;

    private final PublishSubject<Project> currentProject = PublishSubject.create();
    private final PublishSubject<Void> projectSwitcherClicked = PublishSubject.create();

    private final Observable<DateTime> projectLaunchDate;
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
    public Observable<DateTime> projectLaunchDate() {
      return this.projectLaunchDate;
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
