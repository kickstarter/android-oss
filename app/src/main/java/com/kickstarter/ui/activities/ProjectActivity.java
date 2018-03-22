package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.data.LoginReason;
import com.kickstarter.viewmodels.ProjectViewModel;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresActivityViewModel(ProjectViewModel.ViewModel.class)
public final class ProjectActivity extends BaseActivity<ProjectViewModel.ViewModel> {
  private ProjectAdapter adapter;

  protected @Bind(R.id.project_recycler_view) RecyclerView projectRecyclerView;
  protected @Bind(R.id.heart_icon) ImageButton heartButton;
  protected @Bind(R.id.back_project_button) Button backProjectButton;
  protected @Bind(R.id.manage_pledge_button) Button managePledgeButton;
  protected @Bind(R.id.project_action_buttons) ViewGroup projectActionButtonsViewGroup;
  protected @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  protected @BindDimen(R.dimen.grid_8) int grid8Dimen;

  protected @BindString(R.string.project_back_button) String projectBackButtonString;
  protected @BindString(R.string.project_checkout_manage_navbar_title) String managePledgeString;
  protected @BindString(R.string.project_accessibility_button_share_label) String projectShareLabelString;
  protected @BindString(R.string.project_share_twitter_message) String projectShareCopyString;
  protected @BindString(R.string.project_star_confirmation) String projectStarConfirmationString;
  protected @BindString(R.string.project_subpages_menu_buttons_campaign) String campaignString;
  protected @BindString(R.string.project_subpages_menu_buttons_creator) String creatorString;
  protected @BindString(R.string.project_subpages_menu_buttons_updates) String updatesString;

  private KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    this.ksString = environment().ksString();

    final int bottomButtonVisibility = ViewUtils.isLandscape(this) ? View.GONE : View.VISIBLE;
    this.projectActionButtonsViewGroup.setVisibility(bottomButtonVisibility);

    this.adapter = new ProjectAdapter(this.viewModel);
    this.projectRecyclerView.setAdapter(this.adapter);
    this.projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewModel.outputs.heartDrawableId()
      .compose(bindToLifecycle())
      .compose(Transformers.observeForUI())
      .subscribe(i -> this.heartButton.setImageDrawable(ContextCompat.getDrawable(this, i)));

    this.viewModel.outputs.projectAndUserCountry()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pc -> this.renderProject(pc.first, pc.second));

    this.viewModel.outputs.startCampaignWebViewActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCampaignWebViewActivity);

    this.viewModel.outputs.startCommentsActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCommentsActivity);

    this.viewModel.outputs.startCreatorBioWebViewActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCreatorBioWebViewActivity);

    this.viewModel.outputs.showShareSheet()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startShareIntent);

    this.viewModel.outputs.startProjectUpdatesActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startProjectUpdatesActivity);

    this.viewModel.outputs.startVideoActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startVideoActivity);

    this.viewModel.outputs.startCheckoutActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCheckoutActivity);

    this.viewModel.outputs.startManagePledgeActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startManagePledge);

    this.viewModel.outputs.startBackingActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startBackingActivity);

    this.viewModel.outputs.showSavedPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.showStarToast());

    this.viewModel.outputs.startLoginToutActivity()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startLoginToutActivity());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.projectRecyclerView.setAdapter(null);
  }

  private void renderProject(final @NonNull Project project, final @NonNull String configCountry) {
    this.adapter.takeProject(project, configCountry);
    ProjectUtils.setActionButton(project, this.backProjectButton, this.managePledgeButton, this.viewPledgeButton);
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    this.viewModel.inputs.backProjectButtonClicked();
  }

  @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    this.viewModel.inputs.managePledgeButtonClicked();
  }

  @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    this.viewModel.inputs.viewPledgeButtonClicked();
  }

  @OnClick(R.id.heart_icon)
  public void starProjectClick() {
    this.viewModel.inputs.heartButtonClicked();
  }

  @OnClick(R.id.share_icon)
  public void shareProjectClick() {
    this.viewModel.inputs.shareButtonClicked();
  }

  private void startCampaignWebViewActivity(final @NonNull Project project) {
    startWebViewActivity(this.campaignString, project.descriptionUrl());
  }

  private void startCreatorBioWebViewActivity(final @NonNull Project project) {
    startWebViewActivity(this.creatorString, project.creatorBioUrl());
  }

  private void startProjectUpdatesActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectUpdatesActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void showStarToast() {
    ViewUtils.showToastFromTop(this, this.projectStarConfirmationString, 0, this.grid8Dimen);
  }

  private void startCheckoutActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.URL, project.newPledgeUrl())
      .putExtra(IntentKey.TOOLBAR_TITLE, this.projectBackButtonString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startManagePledge(final @NonNull Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.URL, project.editPledgeUrl())
      .putExtra(IntentKey.TOOLBAR_TITLE, this.managePledgeString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startCommentsActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, CommentsActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  private void startShareIntent(final @NonNull Project project) {
    final String shareMessage = this.ksString.format(this.projectShareCopyString, "project_title", project.name());

    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, shareMessage + " " + project.webProjectUrl());
    startActivity(Intent.createChooser(intent, this.projectShareLabelString));
  }

  private void startWebViewActivity(final @NonNull String toolbarTitle, final @NonNull String url) {
    final Intent intent = new Intent(this, WebViewActivity.class)
      .putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle)
      .putExtra(IntentKey.URL, url);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.LOGIN_REASON, LoginReason.STAR_PROJECT);
    startActivityForResult(intent, ActivityRequestCodes.LOGIN_FLOW);
  }

  private void startBackingActivity(final @NonNull Pair<Project, User> projectAndBacker) {
    final Intent intent = new Intent(this, BackingActivity.class)
      .putExtra(IntentKey.PROJECT, projectAndBacker.first)
      .putExtra(IntentKey.BACKER, projectAndBacker.second);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startVideoActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, VideoActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivity(intent);
  }

  @Override
  protected @Nullable Pair<Integer, Integer> exitTransition() {
    return Pair.create(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
