package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.SurveyResponseViewModel;

import java.util.Collections;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel.class)
public class SurveyResponseActivity extends BaseActivity<SurveyResponseViewModel.ViewModel> implements KSWebViewClient.Delegate {
  protected @Bind(R.id.survey_response_web_view) KSWebView ksWebView;

  protected @BindString(R.string.Got_it_your_survey_response_has_been_submitted) String surveyResponseSubmittedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.survey_response_layout);
    ButterKnife.bind(this);

    this.ksWebView.client().setDelegate(this);
    this.ksWebView.client().registerRequestHandlers(
      Collections.singletonList(new RequestHandler(KSUri::isProjectSurveyUri, this::handleProjectSurveyUri))
    );

    this.viewModel.outputs.showConfirmationDialog()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> ViewUtils.showDialog(this, null, this.surveyResponseSubmittedString));
  }

  private boolean handleProjectSurveyUri(final @NonNull Request request, final @NonNull WebView webView) {
    // inputs
    return true;
  }

  @Override
  protected void onResume() {
    super.onResume();

    this.viewModel.outputs.webViewUrl()
      .take(1)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.ksWebView::loadUrl);
  }

  // todo: user vs project survey ? == FOR DEEPLINKING

  @Override
  public void webViewExternalLinkActivated(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {

  }

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {

  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {

  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {

  }
}
