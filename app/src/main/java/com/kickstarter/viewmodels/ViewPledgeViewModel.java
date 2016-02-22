package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.models.Backing;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ViewPledgeActivity;
import com.kickstarter.viewmodels.outputs.ViewPledgeViewModelOutputs;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;

public final class ViewPledgeViewModel extends ViewModel<ViewPledgeActivity> implements ViewPledgeViewModelOutputs  {
  private final ApiClientType client;
  private final CurrentUser currentUser;

  private final BehaviorSubject<Backing> backing = BehaviorSubject.create();
  @Override
  public Observable<Backing> backing() {
    return backing;
  }

  public final ViewPledgeViewModelOutputs outputs = this;

  public ViewPledgeViewModel(final @NonNull Environment environment) {
    super(environment);

    client = environment.apiClient();
    currentUser = environment.currentUser();
  }

  @Override
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    final Observable<Project> project = intent
      .map(i -> i.getParcelableExtra(IntentKey.PROJECT))
      .ofType(Project.class);

    final Observable<Pair<ViewPledgeActivity, Backing>> viewAndBacking = view
      .compose(Transformers.takePairWhen(backing))
      .share();

    project
      .compose(Transformers.combineLatestPair(currentUser.observable()))
      .filter(pu -> pu.second != null)
      .switchMap(pu -> fetchProjectBacking(pu.first, pu.second))
      .compose(bindToLifecycle())
      .subscribe(backing::onNext);

    viewAndBacking
      .compose(Transformers.takeWhen(backing))
      .observeOn(AndroidSchedulers.mainThread())
      .compose(bindToLifecycle())
      .subscribe(vb -> vb.first.show(vb.second));
  }

  public Observable<Backing> fetchProjectBacking(final @NonNull Project project, final @NonNull User user) {
    return client.fetchProjectBacking(project, user)
      .retry(3)
      .compose(Transformers.neverError());
  }
}
