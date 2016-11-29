package com.kickstarter.viewmodels;

import android.support.annotation.NonNull;

import com.kickstarter.libs.ActivityViewModel;
import com.kickstarter.libs.Environment;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectSocialActivity;
import com.kickstarter.viewmodels.outputs.ProjectSocialViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class ProjectSocialViewModel extends ActivityViewModel<ProjectSocialActivity> implements ProjectSocialViewModelOutputs {

  private final BehaviorSubject<Project> project = BehaviorSubject.create();
  @Override
  public Observable<Project> project() {
    return project;
  }

  public final ProjectSocialViewModelOutputs outputs = this;

  public ProjectSocialViewModel(final @NonNull Environment environment) {
    super(environment);

    intent()
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class)
      .compose(bindToLifecycle())
      .subscribe(project);
  }
}
