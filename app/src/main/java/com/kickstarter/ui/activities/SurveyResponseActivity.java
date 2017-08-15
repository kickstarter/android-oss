package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.View;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.AnimationUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.SurveyResponseViewModel;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel.class)
public class SurveyResponseActivity extends BaseActivity<SurveyResponseViewModel.ViewModel> implements KSWebViewClient.Delegate {
  private AlertDialog confirmationDialog;

  protected @Bind(R.id.survey_response_web_view) KSWebView ksWebView;
  protected @Bind(R.id.survey_response_loading_indicator_view) View loadingIndicatorView;

  protected @BindString(R.string.general_alert_buttons_ok) String okString;
  protected @BindString(R.string.Got_it_your_survey_response_has_been_submitted) String surveyResponseSubmittedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.survey_response_layout);
    ButterKnife.bind(this);

    this.ksWebView.client().setDelegate(this);
    this.ksWebView.client().registerRequestHandlers(
      Arrays.asList(
        new RequestHandler(KSUri::isProjectSurveyUri, this::handleProjectSurveyUriRequest),
        new RequestHandler(KSUri::isProjectUri, this::handleProjectUriRequest)
      )
    );

    this.viewModel.outputs.goBack()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> back());

    this.viewModel.outputs.showConfirmationDialog()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> lazyConfirmationDialog().show());

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectActivity);
  }

  private boolean handleProjectUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToProjectRequest(request);
    return true;
  }

  private boolean handleProjectSurveyUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    // do we need to intercept this?
    return true;
  }

  private @NonNull AlertDialog lazyConfirmationDialog() {
    if (this.confirmationDialog == null) {
      this.confirmationDialog = new AlertDialog.Builder(this)
        .setMessage(this.surveyResponseSubmittedString)
        .setPositiveButton(this.okString, (__, ___) ->
          this.viewModel.inputs.okButtonClicked()
        )
        .create();
    }
    return this.confirmationDialog;
  }

  private void startProjectActivity(final @NonNull Pair<Project, RefTag> projectAndRefTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, projectAndRefTag.first)
      .putExtra(IntentKey.REF_TAG, projectAndRefTag.second);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
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

  @Override
  public void webViewExternalLinkActivated(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {}

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    this.loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.disappearAnimation());
  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    this.loadingIndicatorView.startAnimation(AnimationUtils.INSTANCE.appearAnimation());
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {

  }
}
