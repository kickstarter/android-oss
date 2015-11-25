package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.KSApplication;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.viewmodels.errors.ViewPledgeViewModelErrors;
import com.kickstarter.services.ApiClient;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.ui.activities.ViewPledgeActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public final class ViewPledgeViewModel extends ViewModel<ViewPledgeActivity> implements ViewPledgeViewModelErrors {
  private final PublishSubject<Project> project = PublishSubject.create();

  @Inject ApiClient client;
  @Inject CurrentUser currentUser;

  // Errors
  private PublishSubject<ErrorEnvelope> backingLoadFailed = PublishSubject.create();
  public Observable<Void> backingLoadFailed() {
    return backingLoadFailed.map(__ -> null);
  }

  @Override
  protected void onCreate(@NonNull final Context context, @Nullable final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);
    ((KSApplication) context.getApplicationContext()).component().inject(this);

    final Observable<Backing> backing = project
      .compose(Transformers.combineLatestPair(currentUser.observable()))
      .filter(pu -> pu.second != null)
      .switchMap(pu -> fetchProjectBacking(pu.first, pu.second))
      .share();

    final Observable<Pair<ViewPledgeActivity, Backing>> viewAndBacking = viewSubject
      .compose(Transformers.takePairWhen(backing))
      .share();

    addSubscription(viewAndBacking
        .compose(Transformers.takeWhen(backing))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(vb -> vb.first.show(vb.second))
    );
  }

  public void initialize(@NonNull final Project project) {
    this.project.onNext(project);
  }

  public Observable<Backing> fetchProjectBacking(@NonNull final Project project, @NonNull final User user) {
    return client.fetchProjectBacking(project, user)
      .compose(Transformers.pipeApiErrorsTo(backingLoadFailed));
  }
}
