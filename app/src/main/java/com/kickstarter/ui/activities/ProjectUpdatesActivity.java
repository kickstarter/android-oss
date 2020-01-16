package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.ProjectUpdatesViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(ProjectUpdatesViewModel.ViewModel.class)
public class ProjectUpdatesActivity extends BaseActivity<ProjectUpdatesViewModel.ViewModel> implements KSWebView.Delegate {
  protected @Bind(R.id.web_view) KSWebView ksWebView;
  protected @Bind(R.id.web_view_toolbar) KSToolbar webViewToolbar;

  protected @BindString(R.string.project_subpages_menu_buttons_updates) String updatesTitleString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.web_view_layout);
    ButterKnife.bind(this);

    this.webViewToolbar.setTitle(this.updatesTitleString);

    this.ksWebView.setDelegate(this);
    this.ksWebView.registerRequestHandlers(
      Arrays.asList(
        new RequestHandler(KSUri::isProjectUpdatesUri, this::handleProjectUpdatesUriRequest),
        new RequestHandler(KSUri::isProjectUpdateCommentsUri, this::handleProjectUpdateCommentsUriRequest),
        new RequestHandler(KSUri::isProjectUpdateUri, this::handleProjectUpdateUriRequest)
      )
    );

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

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.ksWebView.setDelegate(null);
  }

  private boolean handleProjectUpdateCommentsUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToCommentsRequest(request);
    return true;
  }

  private boolean handleProjectUpdateUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToUpdateRequest(request);
    return true;
  }

  private boolean handleProjectUpdatesUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToUpdatesRequest(request);
    return false;
  }

  private void startCommentsActivity(final @NonNull Update update) {
    final Intent intent = new Intent(this, CommentsActivity.class)
      .putExtra(IntentKey.UPDATE, update);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startUpdateActivity(final @NonNull Project project, final @NonNull Update update) {
    final Intent intent = new Intent(this, UpdateActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.UPDATE, update);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @Override
  public void externalLinkActivated(final @NotNull String url) {
    this.viewModel.inputs.externalLinkActivated();
  }

  @Override
  public void pageIntercepted(final @NotNull String url) {}

  @Override
  public void onReceivedError(final @NotNull String url) {}

}
