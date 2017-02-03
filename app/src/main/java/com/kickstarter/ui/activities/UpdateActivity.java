package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.Update;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(Update.ViewModel.class)
public class UpdateActivity extends BaseActivity<Update.ViewModel> implements KSWebViewClient.Delegate {
  protected @Bind(R.id.update_web_view) KSWebView ksWebView;
  protected @Bind(R.id.loading_indicator_view) View loadingIndicatorView;
  protected @Bind(R.id.update_toolbar) KSToolbar toolbar;

  protected @BindString(R.string.social_update_number) String updateNumberString;

  private KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.update_layout);
    ButterKnife.bind(this);

    this.ksString = environment().ksString();
    this.ksWebView.client().setDelegate(this);

    this.viewModel.outputs.webViewUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.ksWebView::loadUrl);

    this.viewModel.outputs.updateSequence()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setToolbarTitle);
  }

  private void setToolbarTitle(final @NonNull String updateSequence) {
    this.toolbar.setTitle(ksString.format(updateNumberString, "update_number", updateSequence));
  }

  @OnClick(R.id.share_icon_button)
  public void shareIconButtonPressed() {}

  @Override
  public void webViewOnPageFinished(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    // todo: maybe we should reuse these indicator animations for all our webviews
    final AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    this.loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewOnPageStarted(final @NonNull KSWebViewClient webViewClient, final @Nullable String url) {
    final AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(300L);
    animation.setFillAfter(true);
    this.loadingIndicatorView.startAnimation(animation);
  }

  @Override
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {}
}
