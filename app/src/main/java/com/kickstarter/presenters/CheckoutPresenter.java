package com.kickstarter.presenters;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebView;

import com.kickstarter.libs.ApiCapabilities;
import com.kickstarter.libs.Presenter;
import com.kickstarter.libs.RxUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KickstarterUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.activities.CheckoutActivity;
import com.kickstarter.ui.views.KickstarterWebView;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class CheckoutPresenter extends Presenter<CheckoutActivity> {
  private Project project;
  private final PublishSubject<Void> loginSuccess = PublishSubject.create();
  private final PublishSubject<String> url = PublishSubject.create();

  @Override
  protected void onCreate(final Context context, final Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    addSubscription(RxUtils.combineLatestPair(viewSubject(), url)
      .take(1)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(vu -> startWebView(vu.first, vu.second)));

    addSubscription(RxUtils.combineLatestPair(viewChange, loginSuccess)
      .filter(viewChangeAndLoginSuccess -> viewChangeAndLoginSuccess.first != null)
      .take(1)
      .map(viewChangeAndLoginSuccess -> viewChangeAndLoginSuccess.first)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::checkoutNext));
  }

  public void initialize(final Project project, final String url) {
    this.project = project;
    this.url.onNext(url);
  }

  public void takeLoginSuccess() {
    loginSuccess.onNext(null);
  }

  private void startWebView(final CheckoutActivity activity, final String url) {
    final KickstarterWebView webView = activity.webView;

    webView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KickstarterUri::isCheckoutThanksUri, this::handleCheckoutThanksUriRequest),
      new RequestHandler(KickstarterUri::isSignupUri, this::handleSignupUriRequest)
    ));

    webView.loadUrl(url);
  }

  private boolean handleSignupUriRequest(final Request request, final WebView webView) {
    ((CheckoutActivity) webView.getContext()).startLoginToutActivity();
    return true;
  }

  private boolean handleCheckoutThanksUriRequest(final Request request, final WebView webView) {
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
