package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.models.Reward;
import com.kickstarter.presenters.ProjectPresenter;
import com.kickstarter.ui.adapters.ProjectAdapter;
import com.kickstarter.ui.views.IconTextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresPresenter(ProjectPresenter.class)
public class ProjectActivity extends BaseActivity<ProjectPresenter> {
  private ProjectAdapter adapter;

  @Bind(R.id.rewards_recycler_view) RecyclerView rewardsRecyclerView;
  @Bind(R.id.star_icon) IconTextView starIconTextView;
  @Bind(R.id.back_project_button) Button backProjectButton;
  @Bind(R.id.manage_pledge_button) Button managePledgeButton;
  @Bind(R.id.view_pledge_button) Button viewPledgeButton;

  @Inject Money money;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    final String param = intent.getStringExtra(getString(R.string.intent_project_param));
    presenter.initialize(project, param);

    setProjectActionButton(project);
    adapter = new ProjectAdapter(presenter);
    rewardsRecyclerView.setAdapter(adapter);
    rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  public void setProjectActionButton(@NonNull final Project project) {
    if (project.isBacking()) {
      backProjectButton.setVisibility(View.GONE);
      if (project.isLive()) {
        managePledgeButton.setVisibility(View.VISIBLE);
        viewPledgeButton.setVisibility(View.GONE);
      } else {
        viewPledgeButton.setVisibility(View.VISIBLE);
        managePledgeButton.setVisibility(View.GONE);
      }
    } else {
      backProjectButton.setVisibility(View.VISIBLE);
      managePledgeButton.setVisibility(View.GONE);
      viewPledgeButton.setVisibility(View.GONE);
    }
  }

  public void show(@NonNull final Project project) {
    final int starColor = (project.isStarred()) ? R.color.green : R.color.dark_gray;
    starIconTextView.setTextColor(ContextCompat.getColor(this, starColor));
    adapter.takeProject(project);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    presenter.takeBackProjectClick();
  }

  @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    presenter.takeManagePledgeClick();
  }

  @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    presenter.takeViewPledgeClick();
  }

  public void managePledge(@NonNull final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.editPledgeUrl())
      .putExtra(getString(R.string.intent_toolbar_title), getString(R.string.Manage_pledge));
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @OnClick(R.id.star_icon)
  public void starProjectClick() {
    presenter.takeStarClick();
  }

  @OnClick(R.id.share_icon)
  public void shareProject() {
    presenter.takeShareClick();
  }

  public void showProjectDescription(@NonNull final Project project) {
    startWebViewActivity(project.descriptionUrl());
  }

  public void showCreatorBio(@NonNull final Project project) {
    startWebViewActivity(project.creatorBioUrl());
  }

  public void showUpdates(@NonNull final Project project) {
    startWebViewActivity(project.updatesUrl());
  }

  public void showStarPrompt() {
    final Toast toast = Toast.makeText(this, R.string.Well_remind_you_48_hours, Toast.LENGTH_LONG);
    toast.show();
  }

  public void startCheckoutActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.newPledgeUrl())
      .putExtra(getString(R.string.intent_toolbar_title), getString(R.string.Back_this_project));
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startCommentsActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, CommentFeedActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startRewardSelectedCheckout(@NonNull final Project project, @NonNull final Reward reward) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.rewardSelectedUrl(reward));
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  public void startShareIntent(@NonNull final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType(getString(R.string.intent_share_type))
      .putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_message), project.name(), project.webProjectUrl()));
    startActivity(intent);
  }

  private void startWebViewActivity(@NonNull final String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), url);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startLoginToutActivity() {
    final Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent, ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  public void startViewPledgeActivity(@NonNull final Project project) {
    final Intent intent = new Intent(this, ViewPledgeActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, @NonNull final Intent intent) {
    if (requestCode != ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED) {
      return;
    }
    if (resultCode != RESULT_OK) {
      return;
    }
    presenter.takeLoginSuccess();
  }
}
