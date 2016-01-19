package com.kickstarter.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.widget.ShareDialog;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.vendor.TweetComposer;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ThanksAdapter;
import com.kickstarter.viewmodels.ThanksViewModel;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(ThanksViewModel.class)
public final class ThanksActivity extends BaseActivity<ThanksViewModel> {
  protected @Bind(R.id.backed_project) TextView backedProjectTextView;
  protected @Bind(R.id.recommended_projects_recycler_view) RecyclerView recommendedProjectsRecyclerView;
  protected @Bind(R.id.woohoo_background) ImageView woohooBackgroundImageView;

  protected @BindString(R.string.project_checkout_share_twitter_I_just_backed_project_on_kickstarter) String iJustBackedString;
  protected @BindString(R.string.project_accessibility_button_share_label) String shareThisProjectString;
  protected @BindString(R.string.project_checkout_share_you_just_backed_project_share_this_project_html) String youJustBackedString;

  public CallbackManager facebookCallbackManager;
  public ShareDialog shareDialog;

  @Inject KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.thanks_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    facebookCallbackManager = CallbackManager.Factory.create(); // TODO: Use this to track Facebook shares
    shareDialog = new ShareDialog(this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    recommendedProjectsRecyclerView.setLayoutManager(layoutManager);

    displayWoohooBackground();

    viewModel.takeProject(getIntent().getExtras().getParcelable(IntentKey.PROJECT));
  }

  public void show(final @NonNull Project project) {
    backedProjectTextView.setText(Html.fromHtml(ksString.format(youJustBackedString, "project_name", project.name())));
  }

  public void showRecommended(final @NonNull List<Project> projects, final @NonNull Category category) {
    recommendedProjectsRecyclerView.setAdapter(new ThanksAdapter(projects, category, viewModel));
  }

  @OnClick(R.id.close_button)
  protected void closeButtonClick() {
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  @OnClick(R.id.share_button)
  public void onShareClick() {
    viewModel.takeShareClick();
  }

  @OnClick(R.id.facebook_button)
  public void onFacebookButtonClick(final @NonNull View view) {
    viewModel.takeFacebookClick();
  }

  @OnClick(R.id.twitter_button)
  public void onTwitterButtonClick(final @NonNull View view) {
    viewModel.takeTwitterClick();
  }

  public void startFacebookShareIntent(final @NonNull Project project) {
    if (!ShareDialog.canShow(ShareLinkContent.class)) {
      return;
    }

    final ShareOpenGraphObject object = new ShareOpenGraphObject.Builder()
      .putString("og:type", "kickstarter:project")
      .putString("og:title", project.name())
      .putString("og:description", project.blurb())
      .putString("og:image", project.photo().small())
      .putString("og:url", project.webProjectUrl())
      .build();

    final ShareOpenGraphAction action = new ShareOpenGraphAction.Builder()
      .setActionType("kickstarter:back")
      .putObject("project", object)
      .build();

    final ShareOpenGraphContent content = new ShareOpenGraphContent.Builder()
      .setPreviewPropertyName("project")
      .setAction(action)
      .build();

    shareDialog.show(content);
  }

  public void startShareIntent(final @NonNull Project project) {
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .setType("text/plain")
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
      .putExtra(Intent.EXTRA_TEXT, shareString(project) + " " + project.webProjectUrl());

    startActivity(Intent.createChooser(intent, shareThisProjectString));
  }

  public void startTwitterShareIntent(final @NonNull Project project) {
    new TweetComposer.Builder(this)
      .text(shareString(project))
      .url(project.shareUrl())
      .show();
  }

  public void startDiscoveryCategoryIntent(final @NonNull Category category) {
    final DiscoveryParams params = DiscoveryParams.builder().category(category).build();
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .putExtra(IntentKey.DISCOVERY_PARAMS, params)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  public void startProjectIntent(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, RefTag.thanks());
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private String shareString(final @NonNull Project project) {
    return ksString.format(iJustBackedString, "project_name", project.name());
  }

  private void displayWoohooBackground() {
    new Handler().postDelayed(() -> {
      woohooBackgroundImageView.animate().setDuration(Long.parseLong(getString(R.string.woohoo_duration))).alpha(1);
      final Drawable drawable = woohooBackgroundImageView.getDrawable();
      if (drawable instanceof Animatable) {
        ((Animatable) drawable).start();
      }
    }, 500);
  }
}
