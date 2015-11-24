package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.Presenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.inputs.ProfilePresenterInputs;
import com.kickstarter.presenters.outputs.ProfilePresenterOutputs;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.DiscoverEnvelope;
import com.kickstarter.ui.activities.ProfileActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class ProfilePresenter extends Presenter<ProfileActivity> implements ProfilePresenterInputs, ProfilePresenterOutputs {
  // INPUTS
  // next page

  // OUTPUTS
  private final PublishSubject<List<Project>> projects = PublishSubject.create();
  @Override public Observable<List<Project>> projects() {
    return projects.asObservable();
  }

  // ERRORS?

  @Inject ApiClient apiClient;

  public final ProfilePresenterInputs inputs = this;
  public final ProfilePresenterOutputs outputs = this;

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<List<Project>> backedProjects = apiClient.fetchBackedProjects(20)
      .retry(3)
      .onErrorResumeNext(e -> Observable.empty())
      .map(DiscoverEnvelope::projects);

    backedProjects.subscribe(projects);
  }
}
