package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectSocialActivity;
import com.kickstarter.viewmodels.outputs.ProjectSocialViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class ProjectSocialViewModel extends ViewModel<ProjectSocialActivity> implements ProjectSocialViewModelOutputs {

  private final BehaviorSubject<Project> project = BehaviorSubject.create();
  @Override
  public Observable<Project> project() {
    return project;
  }

  public final ProjectSocialViewModelOutputs outputs = this;

  public ProjectSocialViewModel(final @NonNull Environment environment) {
    super(environment);
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    addSubscription(
      intent()
        .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
        .ofType(Project.class)
        .subscribe(project::onNext)
    );
  }
}
