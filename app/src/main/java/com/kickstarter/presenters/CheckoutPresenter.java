package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterUri;
import com.kickstarter.services.ResponseHandler;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.views.KickstarterWebView;
import com.squareup.okhttp.Response;

import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CheckoutPresenter extends Presenter<CheckoutActivity> {
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();

  private Project project;
  private String url;

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    addSubscription(viewSubject()
        .take(1)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::startWebView));

    addSubscription(RxUtils.combineLatestPair(viewChange, loginSuccess)
      .filter(viewChangeAndLoginSuccess -> viewChangeAndLoginSuccess.first != null)
      .take(1)
      .map(viewChangeAndLoginSuccess -> viewChangeAndLoginSuccess.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::checkoutNext));
  }

  public void initialize(final Project project, final String url) {
    this.project = project;
    this.url = url;
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  private void startWebView(final CheckoutActivity activity) {
    final KickstarterWebView webView = activity.webView;

    webView.client().registerResponseHandlers(Arrays.asList(
      new ResponseHandler(KickstarterUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new ResponseHandler(KickstarterUri::isSignupUri, this::handleSignupUriRequest)
    ));

    webView.loadUrl(url);
  }

  private boolean handleSignupUriRequest(final Response response, final WebView webView) {
    ((CheckoutActivity) webView.getContext()).startLoginToutActivity();
    return true;
  }

  private boolean handleCheckoutThanksUriRequest(final Response response, final WebView webView) {
    ((CheckoutActivity) webView.getContext()).startThanksActivity(project);
    return true;
  }

  private void checkoutNext(final CheckoutActivity activity) {
    final String javascript = "root.checkout_next();";
    if (ApiCapabilities.evaluateJavascript()) {
      activity.webView.evaluateJavascript(javascript, null);
    } else {
      activity.webView.loadUrl("javascript:" + javascript);
    }
  }
}
