package com.kickstarter.viewmodels;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectSocialActivity;

import androidx.annotation.NonNull;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public interface ProjectSocialViewModel {

  interface Outputs {
    Observable<Project> project();
  }

  final class ViewModel extends ActivityViewModel<ProjectSocialActivity> implements Outputs {

    public ViewModel(final @NonNull Environment environment) {
      super(environment);

      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .compose(bindToLifecycle())
        .subscribe(this.project);
    }

    private final BehaviorSubject<Project> project = BehaviorSubject.create();

    public final Outputs outputs = this;

    @Override public @NonNull Observable<Project> project() {
      return this.project;
    }
  }
}
