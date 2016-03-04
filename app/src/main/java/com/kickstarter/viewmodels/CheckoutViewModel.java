package com.kickstarter.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.libs.Environment;
import com.kickstarter.libs.ViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.viewmodels.inputs.CheckoutViewModelInputs;
import com.kickstarter.viewmodels.outputs.CheckoutViewModelOutputs;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public final class CheckoutViewModel extends ViewModel<CheckoutActivity> implements CheckoutViewModelInputs, CheckoutViewModelOutputs {
  // INPUTS
  private PublishSubject<String> pageIntercepted = PublishSubject.create();
  @Override
  public void pageIntercepted(final @NonNull String str) {
    pageIntercepted.onNext(str);
  }

  // OUTPUTS
  private BehaviorSubject<Project> project = BehaviorSubject.create();
  @Override
  public @NonNull Observable<Project> project() {
    return project;
  }

  private BehaviorSubject<String> title = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> title() {
    return title;
  }

  private BehaviorSubject<String> url = BehaviorSubject.create();
  @Override
  public @NonNull Observable<String> url() {
    return url;
  }

  public final CheckoutViewModelInputs inputs = this;
  public final CheckoutViewModelOutputs outputs = this;

  public CheckoutViewModel(final @NonNull Environment environment) {
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

    addSubscription(
      intent()
        .map(i -> i.getStringExtra(IntentKey.TOOLBAR_TITLE))
        .ofType(String.class)
        .subscribe(title::onNext)
    );

    addSubscription(
      intent()
        .map(i -> i.getStringExtra(IntentKey.URL))
        .ofType(String.class)
        .take(1)
        .mergeWith(pageIntercepted)
        .subscribe(url::onNext)
    );
  }
}
