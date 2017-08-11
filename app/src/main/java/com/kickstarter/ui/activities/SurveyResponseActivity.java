package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.SurveyResponseViewModel;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(SurveyResponseViewModel.ViewModel.class)
public class SurveyResponseActivity extends BaseActivity<SurveyResponseViewModel.ViewModel> {
  protected @Bind(R.id.survey_response_web_view) KSWebView ksWebView;

  protected @BindString(R.string.Got_it_your_survey_response_has_been_submitted) String surveyResponseSubmittedString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.survey_response_layout);
    ButterKnife.bind(this);

    this.viewModel.outputs.showConfirmationDialog()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> ViewUtils.showDialog(this, null, this.surveyResponseSubmittedString));
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
