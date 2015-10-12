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
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.vendor.TweetComposer;
import com.kickstarter.models.Category;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ThanksPresenter;
import com.kickstarter.ui.adapters.ThanksAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@RequiresPresenter(ThanksPresenter.class)
public class ThanksActivity extends BaseActivity<ThanksPresenter> {
  @Bind(R.id.backed_project) TextView backedProjectTextView;
  @Bind(R.id.recommended_projects_recycler_view) RecyclerView recommendedProjectsRecyclerView;
  @Bind(R.id.woohoo_background) ImageView woohooBackgroundImageView;

  CallbackManager facebookCallbackManager;
  ThanksAdapter adapter;
  ShareDialog shareDialog;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.thanks_layout);
    ButterKnife.bind(this);

    facebookCallbackManager = CallbackManager.Factory.create(); // TODO: Use this to track Facebook shares
    shareDialog = new ShareDialog(this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    recommendedProjectsRecyclerView.setLayoutManager(layoutManager);

    displayWoohooBackground();

    presenter.takeProject(getIntent().getExtras().getParcelable(getString(R.string.intent_project)));
  }

  public void show(@NonNull final Project project) {
    backedProjectTextView.setText(Html.fromHtml(getString(R.string.You_just_backed, project.name())));
  }

  public void showRecommended(@NonNull final List<Project> projects, @NonNull final Category category) {
    adapter = new ThanksAdapter(projects, category, presenter);
    recommendedProjectsRecyclerView.setAdapter(adapter);
  }

  @OnClick(R.id.done_button)
  public void onDoneClick() {
    presenter.takeDoneClick();
  }

  @OnClick(R.id.share_button)
  public void onShareClick() {
    presenter.takeShareClick();
  }

  @OnClick(R.id.facebook_button)
  public void onFacebookButtonClick(@NonNull final View view) {
    presenter.takeFacebookClick();
  }

  @OnClick(R.id.twitter_button)
  public void onTwitterButtonClick(@NonNull final View view) {
    presenter.takeTwitterClick();
  }

  public void startFacebookShareIntent(@NonNull final Project project) {
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

  public void startShareIntent(@NonNull final Project project) {
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND)
      .setType("text/plain")
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
      .putExtra(Intent.EXTRA_TEXT, shareString(project));

    startActivity(Intent.createChooser(intent, getString(R.string.Share_this_project)));
  }

  public void startTwitterShareIntent(@NonNull final Project project) {
    new TweetComposer.Builder(this).text(shareString(project)).show();
  }

  public void startDiscoveryCategoryIntent(@NonNull final Category category) {
    Timber.d("Category name: " + category.name()); // TODO
  }

  public void startDiscoveryActivity() {
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  public void startProjectIntent(@NonNull final Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private String shareString(@NonNull final Project project) {
    return getString(R.string.I_just_backed_project_on_Kickstarter, project.name(), project.secureWebProjectUrl());
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
