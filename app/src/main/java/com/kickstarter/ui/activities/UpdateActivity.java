package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.webkit.WebView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.services.KSUri;
import com.kickstarter.services.RequestHandler;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.toolbars.KSToolbar;
import com.kickstarter.ui.views.KSWebView;
import com.kickstarter.viewmodels.UpdateViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Request;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.TransitionUtils.slideInFromLeft;

@RequiresActivityViewModel(UpdateViewModel.ViewModel.class)
public class UpdateActivity extends BaseActivity<UpdateViewModel.ViewModel> implements KSWebView.Delegate {
  protected @Bind(R.id.update_web_view) KSWebView ksWebView;
  protected @Bind(R.id.update_toolbar) KSToolbar toolbar;

  protected @BindString(R.string.social_update_number) String updateNumberString;
  protected @BindString(R.string.activity_project_update_update_count) String shareUpdateCountString;

  private KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.update_layout);
    ButterKnife.bind(this);

    this.ksString = environment().ksString();

    this.ksWebView.setDelegate(this);
    this.ksWebView.registerRequestHandlers(
      Arrays.asList(
        new RequestHandler(KSUri::isProjectUpdateUri, this::handleProjectUpdateUriRequest),
        new RequestHandler(KSUri::isProjectUpdateCommentsUri, this::handleProjectUpdateCommentsUriRequest),
        new RequestHandler(KSUri::isProjectUri, this::handleProjectUriRequest)
      )
    );

    this.viewModel.outputs.startCommentsActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startCommentsActivity);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));

    this.viewModel.outputs.startShareIntent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startShareIntent);

    this.viewModel.outputs.updateSequence()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setToolbarTitle);
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
    return false;
  }

  private boolean handleProjectUriRequest(final @NonNull Request request, final @NonNull WebView webView) {
    this.viewModel.inputs.goToProjectRequest(request);
    return true;
  }

  private void setToolbarTitle(final @NonNull String updateSequence) {
    this.toolbar.setTitle(this.ksString.format(this.updateNumberString, "update_number", updateSequence));
  }

  private void startCommentsActivity(final @NonNull Update update) {
    final Intent intent = new Intent(this, CommentsActivity.class)
      .putExtra(IntentKey.UPDATE, update);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startShareIntent(final @NonNull Update update) {
    final String shareMessage = this.ksString.format(this.shareUpdateCountString, "update_count", NumberUtils.format(update.sequence()))
      + ": " + update.title();

    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, shareMessage + " " + update.urls().web().update());
    startActivity(intent);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return slideInFromLeft();
  }

  @OnClick(R.id.share_icon_button)
  public void shareIconButtonPressed() {
    this.viewModel.inputs.shareIconButtonClicked();
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
