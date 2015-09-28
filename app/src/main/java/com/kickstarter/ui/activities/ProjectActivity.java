package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.ActivityRequestCodes;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.models.Video;
import com.kickstarter.presenters.ProjectPresenter;
import com.kickstarter.ui.views.IconTextView;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

@RequiresPresenter(ProjectPresenter.class)
public class ProjectActivity extends BaseActivity<ProjectPresenter> {
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.project_detail_photo) ImageView photoImageView;
  protected @Bind(R.id.project_detail_video) VideoView videoView;
  protected @Bind(R.id.play_button_overlay) IconTextView playButtonIconTextView;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.avatar_name) TextView avatarNameTextView;
  protected @Bind(R.id.fund_message) TextView fundMessageTextView;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;
  protected @Bind(R.id.star_icon) IconTextView starIconTextView;

  @Inject Money money;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    final String param = intent.getStringExtra(getString(R.string.intent_project_param));
    presenter.initialize(project, param);
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
//      loadVideo(project.video(), videoView);
      playButtonIconTextView.setVisibility(View.VISIBLE);
    }
    else {
      playButtonIconTextView.setVisibility(View.GONE);
    }

    // Creator information
    Picasso.with(this).load(project.creator().avatar()
      .medium())
      .transform(new CircleTransform())
      .into(avatarImageView);
    avatarNameTextView.setText(project.creator().name());
    fundMessageTextView.setText(String.format(getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
    updatesCountTextView.setText(project.formattedUpdatesCount());
    commentsCountTextView.setText(project.formattedCommentsCount());

    int starColor = (project.isStarred()) ? R.color.green : R.color.dark_gray;
    starIconTextView.setTextColor(ContextCompat.getColor(this, starColor));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", this.toString());

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  // todo: setting the VideoView uri here prevents the activity from GC'ing
  // VideoView either needs to be set independent of XML, or better
  // just use ExoPlayer library
  public void loadVideo(Video video, VideoView videoView) {
    final Uri videoUri = Uri.parse(video.base());
    videoView.setVideoURI(videoUri);
    videoView.setMediaController(new MediaController(this));

    // replace with @OnClick
    playButtonIconTextView.setOnClickListener((View v) -> {
      photoImageView.setVisibility(View.GONE);
      playButtonIconTextView.setVisibility(View.GONE);
      videoView.start();
    });
  }

  @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    presenter.takeBackProjectClick();
  }

  @OnClick(R.id.comments)
  public void commentsClick() {
    presenter.takeCommentsClick();
  }

  @OnClick(R.id.star_icon)
  public void starProjectClick() {
    presenter.takeStarClick();
  }

  @OnClick(R.id.updates)
  public void updatesClick() {
    presenter.takeUpdatesClick();
  }

  @OnClick({R.id.blurb, R.id.campaign})
  public void blurbOnClick() {
    presenter.takeBlurbClick();
  }

  @OnClick({R.id.creator_name, R.id.creator_info})
  public void creatorNameOnClick() {
    presenter.takeCreatorNameClick();
  }

  @OnClick({R.id.share_icon, R.id.share_button})
  public void shareProject() {
    presenter.takeShareClick();
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

  public void showStarPrompt() {
    final Toast toast = Toast.makeText(this, R.string.Well_remind_you_48_hours, Toast.LENGTH_LONG);
    toast.show();
  }

  public void startCheckoutActivity(final Project project) {
    final Intent intent = new Intent(this, CheckoutActivity.class)
      .putExtra(getString(R.string.intent_project), project)
      .putExtra(getString(R.string.intent_url), project.newPledgeUrl());
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: WORKING ON:
  public void startCommentsActivity(final Project project) {
    final Intent intent = new Intent(this, CommentFeedActivity.class)
      .putExtra(getString(R.string.intent_project), project);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  // todo: limit the apps you can share to
  public void startShareIntent(final Project project) {
    final Intent intent = new Intent(Intent.ACTION_SEND)
      .setType(getString(R.string.intent_share_type))
      .putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_message), project.name(), project.webProjectUrl()));
    startActivity(intent);
  }

  private void startWebViewActivity(final String url) {
    final Intent intent = new Intent(this, DisplayWebViewActivity.class)
      .putExtra(getString(R.string.intent_url), url);
    startActivity(intent);
    overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  public void startLoginToutActivity() {
    Intent intent = new Intent(this, LoginToutActivity.class)
      .putExtra(getString(R.string.intent_forward), true);
    startActivityForResult(intent, ActivityRequestCodes.PROJECT_ACTIVITY_LOGIN_TOUT_ACTIVITY_USER_REQUIRED);
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    if (resultCode != RESULT_OK) {
      finish();
    } else {
      presenter.takeLoginSuccess();
    }
  }
}
