package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.KSWebViewClient;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.ProjectUpdates;

import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(ProjectUpdates.ViewModel.class)
public class ProjectUpdatesActivity extends BaseActivity<ProjectUpdates.ViewModel> implements KSWebViewClient.Delegate {
  protected @Bind(R.id.web_view) KSWebView ksWebView;
  protected @Bind(R.id.loading_indicator_view) View loadingIndicatorView;
  protected @Bind(R.id.web_view_toolbar) KSToolbar webViewToolbar;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    this.webViewToolbar.setTitle(R.string.project_subpages_menu_buttons_updates);

    this.ksWebView.client().setDelegate(this);
    this.ksWebView.client().registerRequestHandlers(Arrays.asList(
      new RequestHandler(KSUri::isProjectUpdateCommentsUri, this::handleProjectUpdateCommentsUriRequest),
      new RequestHandler(KSUri::isProjectUpdateUri, this::handleProjectUpdateUriRequest)
    ));

    this.viewModel.outputs.startCommentsActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startCommentsActivity);

    this.viewModel.outputs.startUpdateActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pu -> this.startUpdateActivity(pu.first, pu.second));
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

  private boolean handleProjectUpdateCommentsUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToCommentsRequest(request);
    return true;
  }

  private boolean handleProjectUpdateUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToUpdateRequest(request);
    return true;
  }

  private void startCommentsActivity(final @NonNull Update update) {
    final Intent intent = new Intent(this, CommentsActivity.class)
      .putExtra(IntentKey.UPDATE, update);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startUpdateActivity(final @NonNull Project project, final @NonNull Update update) {

  }

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
  public void webViewPageIntercepted(final @NonNull KSWebViewClient webViewClient, final @NonNull String url) {
    this.viewModel.inputs.pageInterceptedUrl(url);
  }
}
