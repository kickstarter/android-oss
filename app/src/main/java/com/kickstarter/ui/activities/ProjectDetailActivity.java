package com.kickstarter.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
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
import com.kickstarter.ui.adapters.RewardListAdapter;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ProjectDetailPresenter.class)
public class ProjectDetailActivity extends BaseActivity<ProjectDetailPresenter> {
  RewardListAdapter rewardListAdapter;
  @InjectView(R.id.rewards_recycler_view) RecyclerView rewardsRecyclerView;

  protected @InjectView(R.id.backers_count) TextView backersCount;
  protected @InjectView(R.id.blurb) TextView blurb;
  protected @InjectView(R.id.category) TextView category;
  protected @InjectView(R.id.creator_name) TextView creatorName;
  protected @InjectView(R.id.deadline_countdown) TextView deadlineCountdown;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadlineCountdownUnit;
  protected @InjectView(R.id.goal) TextView goal;
  protected @InjectView(R.id.location) TextView location;
  protected @InjectView(R.id.project_name) TextView projectName;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentageFunded;
  protected @InjectView(R.id.project_detail_photo) ImageView photo;
  protected @InjectView(R.id.project_detail_video) VideoView video;
  protected @InjectView(R.id.play_button_overlay) ImageView playButton;
  protected @InjectView(R.id.pledged) TextView pledged;
  protected @InjectView(R.id.avatar) ImageView avatar;
  protected @InjectView(R.id.avatar_name) TextView avatarName;
  protected @InjectView(R.id.fund_message) TextView fundMessage;

  @Inject Money money;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_detail_layout);
    ButterKnife.inject(this);
    ((KsrApplication) getApplication()).component().inject(this);

    final Intent intent = getIntent();
    final Project project = intent.getExtras().getParcelable("project");
    presenter.takeProject(project);

    rewardsRecyclerView.setHasFixedSize(true);
    rewardsRecyclerView.getLayoutParams().height = 2000; // todo: WIP
    rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  public void show(final Project project) {
    final ClickableSpan readMoreClick = clickSpanToWebView(project.urls().web().description());
    final String readMoreString = getString(R.string.Read_more);
    final SpannableString blurbSpan = new SpannableString(project.blurb() + " " + readMoreString);
    final int readMoreStart = blurbSpan.length() - readMoreString.length();
    blurbSpan.setSpan(new UnderlineSpan(), readMoreStart, blurbSpan.length(), 0);
    blurbSpan.setSpan(readMoreClick, 0, blurbSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    blurb.setText(blurbSpan);
    blurb.setMovementMethod(LinkMovementMethod.getInstance());

    final ClickableSpan creatorNameClick = clickSpanToWebView(project.urls().web().creatorBio());
    final SpannableString creatorNameSpan = new SpannableString(project.creator().name());
    creatorNameSpan.setSpan(new UnderlineSpan(), 0, creatorNameSpan.length(), 0);
    creatorNameSpan.setSpan(creatorNameClick, 0, creatorNameSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    creatorName.setText(creatorNameSpan);
    creatorName.setMovementMethod(LinkMovementMethod.getInstance());

    // Project information
    backersCount.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.backersCount()));
    category.setText(project.category().name());
    deadlineCountdown.setText(Integer.toString(project.deadlineCountdown()));
    deadlineCountdownUnit.setText(project.deadlineCountdownUnit());
    goal.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    location.setText(project.location().displayableName());
    projectName.setText(project.name());
    percentageFunded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    pledged.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    if ( project.video() != null ) {
      loadVideo(project.video(), video);
      playButton.setVisibility(View.VISIBLE);
    }
    else {
      Picasso.with(this).load(project.photo().full()).into(photo);
      playButton.setVisibility(View.GONE);
    }

    // Creator information
    Picasso.with(this).load(project.creator().avatar().medium()).into(avatar);
    avatarName.setText(project.creator().name());
    fundMessage.setText(String.format(getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));

    if (project.rewards() != null) {
      rewardListAdapter = new RewardListAdapter(project.rewards());
      rewardsRecyclerView.setAdapter(rewardListAdapter);  // todo
    }
  }

  // Opens the URL parameter in a KickstarterWebView
  public ClickableSpan clickSpanToWebView(final String url) {
    return new ClickableSpan() {
      @Override
      public void onClick(final View view) {
        final Intent intent = new Intent(view.getContext(), DisplayWebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
      }

      @Override
      public void updateDrawState(final TextPaint link) {
        link.setColor(getResources().getColor(R.color.text_primary));
      }
    };
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", this.toString());
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void closeProjectClick(View v) {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  // todo
  public void starProjectClick(View v) {
  }

  public void loadVideo(Video video, VideoView videoView) {
    Picasso.with(this).load(video.frame()).into(photo); // todo: make this loading smoother
    final Uri video_uri = Uri.parse(video.base());
    videoView.setVideoURI(video_uri);
    videoView.setMediaController(new MediaController(this));

    playButton.setOnClickListener((View v) -> {
      photo.setVisibility(View.GONE);
      playButton.setVisibility(View.GONE);
      videoView.start();
    });
  }

  public void shareOnClick(final View v) {
    presenter.takeShareClick();
  }

  public void backProjectButtonOnClick(final View v) {
    Timber.d("backProjectButtonOnClick");
    presenter.takeBackProjectClick();
  }
}
