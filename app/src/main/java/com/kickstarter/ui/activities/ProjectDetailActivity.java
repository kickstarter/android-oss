package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.presenters.ProjectDetailPresenter;
import com.kickstarter.ui.views.IconTextView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ProjectDetailPresenter.class)
public class ProjectDetailActivity extends BaseActivity<ProjectDetailPresenter> {
  protected @InjectView(R.id.backers_count) TextView backersCount;
  protected @InjectView(R.id.blurb) TextView blurb;
  protected @InjectView(R.id.category) TextView category;
  protected @InjectView(R.id.creator_name) TextView creatorName;
  protected @InjectView(R.id.comments_count) TextView commentsCount;
  protected @InjectView(R.id.deadline_countdown) TextView deadlineCountdown;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadlineCountdownUnit;
  protected @InjectView(R.id.goal) TextView goal;
  protected @InjectView(R.id.location) TextView location;
  protected @InjectView(R.id.project_name) TextView projectName;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentageFunded;
  protected @InjectView(R.id.project_detail_photo) ImageView photo;
  protected @InjectView(R.id.project_detail_video) VideoView video;
  protected @InjectView(R.id.play_button_overlay) IconTextView playButton;
  protected @InjectView(R.id.pledged) TextView pledged;
  protected @InjectView(R.id.avatar) ImageView avatar;
  protected @InjectView(R.id.avatar_name) TextView avatarName;
  protected @InjectView(R.id.fund_message) TextView fundMessage;
  protected @InjectView(R.id.updates_count) TextView updatesCount;

  @Inject Money money;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_detail_layout);
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getExtras().getParcelable(getString(R.string.intent_project));
    presenter.takeProject(project);
  }

  public void show(final Project project) {

    // Project information
    blurb.setText(Html.fromHtml(getString(R.string.Blurb_read_more, project.blurb())));
    creatorName.setText(Html.fromHtml(getString(R.string.by_creator, project.creator().name())));
    backersCount.setText(project.formattedBackersCount());
    category.setText(project.category().name());
    deadlineCountdown.setText(Integer.toString(project.deadlineCountdown()));
    deadlineCountdownUnit.setText(project.deadlineCountdownUnit());
    goal.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    location.setText(project.location().displayableName());
    projectName.setText(project.name());
    percentageFunded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    pledged.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    Picasso.with(this).load(project.photo().full()).into(photo);

    // WIP VideoView & MediaController
    if ( project.video() != null ) {
      loadVideo(project.video(), video);
      playButton.setVisibility(View.VISIBLE);
    }
    else {
      playButton.setVisibility(View.GONE);
    }

    // Creator information
    Picasso.with(this).load(project.creator().avatar().medium()).into(avatar);
    avatarName.setText(project.creator().name());
    fundMessage.setText(String.format(getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
    updatesCount.setText(project.formattedUpdatesCount());
    commentsCount.setText(project.formattedCommentsCount());
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", this.toString());

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void loadVideo(Video video, VideoView videoView) {
    final Uri video_uri = Uri.parse(video.base());
    videoView.setVideoURI(video_uri);
    videoView.setMediaController(new MediaController(this));

    playButton.setOnClickListener((View v) -> {
      photo.setVisibility(View.GONE);
      playButton.setVisibility(View.GONE);
      videoView.start();
    });
  }

  public void backProjectButtonOnClick(final View v) {
    presenter.takeBackProjectClick();
  }

  public void commentsClick(final View v) {
    presenter.takeCommentsClick();
  }

  // todo
  public void starProjectClick(final View v) {
  }

  public void shareOnClick(final View v) {
    presenter.takeShareClick();
  }

  public void updatesClick(final View v) {
    presenter.takeUpdatesClick();
  }

  public void blurbOnClick(final View v) {
    presenter.takeBlurbClick();
  }

  public void creatorNameOnClick(final View v) {
    presenter.takeCreatorNameClick();
  }

  public void showProjectDescription(final Project project) {
    startWebViewActivity(project.descriptionUrl());
  }

  public void showCreatorBio(final Project project) {
    startWebViewActivity(project.creatorBioUrl());
  }

  public void showUpdates(final Project project) {
    startWebViewActivity(project.updatesUrl());
  }

  public void startCheckoutActivity(final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.newPledgeUrl());
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startCommentsActivity(final Project project) {
    // todo: build comments activity
  }

  // todo: limit the apps you can share to
  public void startShareIntent(final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType(getString(R.string.intent_share_type))
      .putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_intent), project.name(), project.webProjectUrl()));
    startActivity(intent);
  }

  private void startWebViewActivity(final String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), url);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
