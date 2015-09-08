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
import com.kickstarter.presenters.ProjectPresenter;
import com.kickstarter.ui.views.IconTextView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ProjectPresenter.class)
public class ProjectActivity extends BaseActivity<ProjectPresenter> {
  protected @InjectView(R.id.backers_count) TextView backersCountTextView;
  protected @InjectView(R.id.blurb) TextView blurbTextView;
  protected @InjectView(R.id.category) TextView categoryTextView;
  protected @InjectView(R.id.creator_name) TextView creatorNameTextView;
  protected @InjectView(R.id.comments_count) TextView commentsCountTextView;
  protected @InjectView(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @InjectView(R.id.goal) TextView goalTextView;
  protected @InjectView(R.id.location) TextView locationTextView;
  protected @InjectView(R.id.project_name) TextView projectNameTextView;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @InjectView(R.id.project_detail_photo) ImageView photoImageView;
  protected @InjectView(R.id.project_detail_video) VideoView videoView;
  protected @InjectView(R.id.play_button_overlay) IconTextView playButtonIconTextView;
  protected @InjectView(R.id.pledged) TextView pledgedTextView;
  protected @InjectView(R.id.avatar) ImageView avatarImageView;
  protected @InjectView(R.id.avatar_name) TextView avatarNameTextView;
  protected @InjectView(R.id.fund_message) TextView fundMessageTextView;
  protected @InjectView(R.id.updates_count) TextView updatesCountTextView;

  @Inject Money money;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_layout);
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getExtras().getParcelable(getString(R.string.intent_project));
    presenter.takeProject(project);
  }

  public void show(final Project project) {

    // Project information
    blurbTextView.setText(Html.fromHtml(getString(R.string.Blurb_read_more, project.blurb())));
    creatorNameTextView.setText(Html.fromHtml(getString(R.string.by_creator, project.creator().name())));
    backersCountTextView.setText(project.formattedBackersCount());
    categoryTextView.setText(project.category().name());
    deadlineCountdownTextView.setText(Integer.toString(project.deadlineCountdownValue()));
    deadlineCountdownUnitTextView.setText(project.deadlineCountdownUnit(this));
    goalTextView.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    locationTextView.setText(project.location().displayableName());
    projectNameTextView.setText(project.name());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    Picasso.with(this).load(project.photo().full()).into(photoImageView);

    // WIP VideoView & MediaController
    if ( project.video() != null ) {
      loadVideo(project.video(), videoView);
      playButtonIconTextView.setVisibility(View.VISIBLE);
    }
    else {
      playButtonIconTextView.setVisibility(View.GONE);
    }

    // Creator information
    Picasso.with(this).load(project.creator().avatar().medium()).into(avatarImageView);
    avatarNameTextView.setText(project.creator().name());
    fundMessageTextView.setText(String.format(getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
    updatesCountTextView.setText(project.formattedUpdatesCount());
    commentsCountTextView.setText(project.formattedCommentsCount());
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", this.toString());

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void loadVideo(Video video, VideoView videoView) {
    final Uri videoUri = Uri.parse(video.base());
    videoView.setVideoURI(videoUri);
    videoView.setMediaController(new MediaController(this));

    playButtonIconTextView.setOnClickListener((View v) -> {
      photoImageView.setVisibility(View.GONE);
      playButtonIconTextView.setVisibility(View.GONE);
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
