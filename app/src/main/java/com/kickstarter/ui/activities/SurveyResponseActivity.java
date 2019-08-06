package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.SurveyResponseViewModel;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel.class)
public class SurveyResponseActivity extends BaseActivity<SurveyResponseViewModel.ViewModel> {
  private AlertDialog confirmationDialog;

  protected @Bind(R.id.survey_response_web_view) KSWebView ksWebView;

  protected @BindString(R.string.general_alert_buttons_ok) String okString;
  protected @BindString(R.string.Got_it_your_survey_response_has_been_submitted) String surveyResponseSubmittedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.survey_response_layout);
    ButterKnife.bind(this);

    this.ksWebView.registerRequestHandlers(
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
  }

  private boolean handleProjectUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.projectUriRequest(request);
    return false;
  }

  private boolean handleProjectSurveyUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.projectSurveyUriRequest(request);
    return false;
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

  @Override
  protected void onResume() {
    super.onResume();

    this.viewModel.outputs.webViewUrl()
      .take(1)
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.ksWebView::loadUrl);
  }
}
