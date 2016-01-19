package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.intents.ProjectIntentAction;
import com.kickstarter.ui.views.IconButton;
import com.kickstarter.viewmodels.ProjectViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ProjectViewModel.class)
public final class ProjectActivity extends BaseActivity<ProjectViewModel> {
  private ProjectAdapter adapter;
  private ProjectIntentAction intentAction;

  protected @Bind(R.id.project_recycler_view) RecyclerView projectRecyclerView;
  protected @Bind(R.id.star_icon) IconButton starButton;
  protected @Bind(R.id.back_project_button) Button backProjectButton;
  protected @Bind(R.id.manage_pledge_button) Button managePledgeButton;
  protected @Bind(R.id.project_action_buttons) ViewGroup projectActionButtonsViewGroup;
  protected @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  protected @BindColor(R.color.green) int green;
  protected @BindColor(R.color.text_primary) int textPrimary;

  protected @BindDimen(R.dimen.grid_8) int grid8Dimen;

  protected @BindString(R.string.project_back_button) String projectBackButtonString;
  protected @BindString(R.string.project_checkout_manage_navbar_title) String managePledgeString;
  protected @BindString(R.string.project_share_twitter_message) String projectShareString;
  protected @BindString(R.string.project_star_confirmation) String projectStarConfirmationString;
  protected @BindString(R.string.project_subpages_menu_buttons_campaign) String campaignString;
  protected @BindString(R.string.project_subpages_menu_buttons_creator) String creatorString;
  protected @BindString(R.string.project_subpages_menu_buttons_updates) String updatesString;

  protected @Inject ApiClientType client;
  protected @Inject KSCurrency ksCurrency;
  protected @Inject KSString ksString;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    final int bottomButtonVisibility = ViewUtils.isLandscape(this) ? View.GONE : View.VISIBLE;
    projectActionButtonsViewGroup.setVisibility(bottomButtonVisibility);

    intentAction = new ProjectIntentAction(viewModel.inputs::initializer, lifecycle(), client);
    intentAction.intent(getIntent());

    adapter = new ProjectAdapter(viewModel);
    projectRecyclerView.setAdapter(adapter);
    projectRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    this.viewModel.inputs.intentRefTag(getIntent().getParcelableExtra(IntentKey.REF_TAG));

    this.viewModel.outputs.projectAndConfig()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(pc -> this.renderProject(pc.first, pc.second.countryCode()));

    this.viewModel.outputs.showCampaign()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showProjectDescription);

    this.viewModel.outputs.showComments()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCommentsActivity);

    this.viewModel.outputs.showCreator()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showCreatorBio);

    this.viewModel.outputs.showShareSheet()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startShareIntent);

    this.viewModel.outputs.showUpdates()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showUpdates);

    this.viewModel.outputs.playVideo()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startVideoPlayerActivity);

    this.viewModel.outputs.startCheckout()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startCheckoutActivity);

    this.viewModel.outputs.startCheckoutWithReward()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        projectAndReward -> this.startRewardSelectedCheckout(projectAndReward.first, projectAndReward.second));

    this.viewModel.outputs.startManagePledge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startManagePledge);

    this.viewModel.outputs.startViewPledge()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startViewPledgeActivity);

    this.viewModel.outputs.showStarredPrompt()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.showStarToast());

    this.viewModel.outputs.showLoginTout()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> this.startLoginToutActivity());
  }

  private void renderProject(final @NonNull Project project, final @NonNull String configCountry) {
    adapter.takeProject(project, configCountry);
    ProjectUtils.setActionButton(project, backProjectButton, managePledgeButton, viewPledgeButton);
    renderStar(project);
  }

  private void renderStar(final @NonNull Project project) {
    final int starColor = (project.isStarred()) ? green : textPrimary;
    starButton.setTextColor(starColor);
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    viewModel.inputs.backProjectClicked();
  }

  @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    viewModel.inputs.managePledgeClicked();
  }

  @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    viewModel.inputs.viewPledgeClicked();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overrideExitTransition();
  }

  private void overrideExitTransition() {
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnClick(R.id.star_icon)
  public void starProjectClick() {
    viewModel.inputs.starClicked();
  }

  @OnClick(R.id.share_icon)
  public void shareProjectClick() {
    viewModel.inputs.shareClicked();
  }

  private void showProjectDescription(final @NonNull Project project) {
    startWebViewActivity(campaignString, project.descriptionUrl());
  }

  private void showCreatorBio(final @NonNull Project project) {
    startWebViewActivity(creatorString, project.creatorBioUrl());
  }

  private void showUpdates(final @NonNull Project project) {
    startWebViewActivity(updatesString, project.updatesUrl());
  }

  private void showStarToast() {
    ViewUtils.showToastFromTop(this, projectStarConfirmationString, 0, grid8Dimen);
  }

  private void startCheckoutActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.URL, project.newPledgeUrl())
      .putExtra(IntentKey.TOOLBAR_TITLE, projectBackButtonString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startManagePledge(final @NonNull Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.URL, project.editPledgeUrl())
      .putExtra(IntentKey.TOOLBAR_TITLE, managePledgeString);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startCommentsActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, CommentFeedActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startRewardSelectedCheckout(final @NonNull Project project, final @NonNull Reward reward) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.TOOLBAR_TITLE, projectBackButtonString)
      .putExtra(IntentKey.URL, project.rewardSelectedUrl(reward));
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  private void startShareIntent(final @NonNull Project project) {
    final String shareMessage = ksString.format(projectShareString, "project_title", project.name());

    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, shareMessage + " " + project.webProjectUrl());
    startActivity(intent);
  }

  private void startWebViewActivity(final @NonNull String toolbarTitle, final @NonNull String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(IntentKey.TOOLBAR_TITLE, toolbarTitle)
      .putExtra(IntentKey.URL, url);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(IntentKey.FORWARD, true)
      .putExtra(IntentKey.LOGIN_TYPE, LoginToutActivity.REASON_STAR_PROJECT);
    startActivityForResult(intent, ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  private void startViewPledgeActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ViewPledgeActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startVideoPlayerActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, VideoPlayerActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final @NonNull Intent intent) {
    if (requestCode != ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    viewModel.inputs.loginSuccess();
  }
}
