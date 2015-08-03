package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.activities.ThanksActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class CheckoutPresenter extends Presenter<CheckoutActivity> {
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();

  private Project project;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    RxUtils.combineLatestPair(viewSubject, loginSuccess)
      .filter(pair -> pair.first != null)
      .take(1)
      .map(pair -> pair.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::checkoutNext);
  }

  public void takeProject(final Project project) {
    this.project = project;
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  public void takeCheckoutThanksUriRequest() {
    final Intent intent = new Intent(view(), ThanksActivity.class);
    intent.putExtra("project", project);
    view().startActivity(intent);
  }

  private void checkoutNext(final CheckoutActivity activity) {
    // In API < 19, can call loadUrl() with string "javascript:fn()"
    activity.webView.evaluateJavascript("root.checkout_next();", null);
  }
}
