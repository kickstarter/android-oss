package com.kickstarter.ui.viewholders;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.TweetComposer;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.ThanksShareHolderViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class ThanksShareViewHolder extends KSViewHolder {
  private final ThanksShareHolderViewModel.ViewModel viewModel;
  private KSString ksString;
  private ShareDialog shareDialog;

  protected @Bind(R.id.backed_project) TextView backedProjectTextView;

  protected @BindString(R.string.project_checkout_share_twitter_I_just_backed_project_on_kickstarter) String iJustBackedString;
  protected @BindString(R.string.project_accessibility_button_share_label) String shareThisProjectString;
  protected @BindString(R.string.You_have_successfully_backed_project_html) String youJustBackedString;

  public ThanksShareViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new ThanksShareHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    this.ksString = environment().ksString();
    this.shareDialog = new ShareDialog((Activity) context());

    this.viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::showBackedProject);

    this.viewModel.outputs.startShare()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startShare);

    this.viewModel.outputs.startShareOnFacebook()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startShareOnFacebook);

    this.viewModel.outputs.startShareOnTwitter()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startShareOnTwitter);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Project project = requireNonNull((Project) data);
    this.viewModel.inputs.configureWith(project);
  }

  @OnClick(R.id.share_button)
  public void shareButtonClicked() {
    this.viewModel.inputs.shareClick();
  }

  @OnClick(R.id.thanks_facebook_share_button)
  public void shareOnFacebookButtonClicked() {
    this.viewModel.inputs.shareOnFacebookClick();
  }

  @OnClick(R.id.thanks_twitter_share_button)
  public void shareOnTwitterButtonClicked() {
    this.viewModel.inputs.shareOnTwitterClick();
  }

  private String shareString(final @NonNull String projectName) {
    return this.ksString.format(this.iJustBackedString, "project_name", projectName);
  }

  private void showBackedProject(final @NonNull String projectName) {
    this.backedProjectTextView.setText(
      Html.fromHtml(this.ksString.format(this.youJustBackedString, "project_name", projectName))
    );
  }

  private void startShare(final @NonNull Pair<String, String> projectNameAndShareUrl) {
    final String projectName = projectNameAndShareUrl.first;
    final String shareUrl = projectNameAndShareUrl.second;
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .setType("text/plain")
      .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
      .putExtra(Intent.EXTRA_TEXT, String.format("%s %s", shareString(projectName), shareUrl));

    context().startActivity(Intent.createChooser(intent, this.shareThisProjectString));
  }

  private void startShareOnFacebook(final @NonNull Pair<Project, String> projectAndShareUrl) {
    if (!ShareDialog.canShow(ShareLinkContent.class)) {
      return;
    }

    final Project project = projectAndShareUrl.first;
    final String shareUrl = projectAndShareUrl.second;
    final Photo photo = project.photo();
    final ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
      .putString("og:type", "kickstarter:project")
      .putString("og:title", project.name())
      .putString("og:description", project.blurb())
      .putString("og:image", photo == null ? null : photo.small())
      .putString("og:url", shareUrl)
      .build();

    final ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
      .setActionType("kickstarter:back")
      .putObject("project", object)
      .build();

    final ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
      .setPreviewPropertyName("project")
      .setAction(action)
      .build();

    this.shareDialog.show(content);
  }

  private void startShareOnTwitter(final @NonNull Pair<String, String> projectNameAndShareUrl) {
    final String projectName = projectNameAndShareUrl.first;
    final String shareUrl = projectNameAndShareUrl.second;
    new TweetComposer.Builder(context())
      .text(shareString(projectName))
      .uri(Uri.parse(shareUrl))
      .show();
  }
}
