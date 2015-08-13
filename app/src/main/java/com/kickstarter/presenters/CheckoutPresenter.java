package com.kickstarter.presenters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kickstarter.libs.ApiCapabilities;
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

    RxUtils.takeWhen(viewSubject, loginSuccess)
      .take(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::checkoutNext);
  }

  public void takeProject(final Project project) {
    this.project = project;
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  public void takeSignupUriRequest() {
    view().startLoginToutActivity();
  }

  public void takeCheckoutThanksUriRequest() {
    view().startThanksActivity(project);
  }

  private void checkoutNext(final CheckoutActivity activity) {
    final String javascript = "root.checkout_next();";
    if (ApiCapabilities.canEvaluateJavascript()) {
      activity.webView.evaluateJavascript(javascript, null);
    } else {
      activity.webView.loadUrl("javascript:" + javascript);
    }
  }
}
