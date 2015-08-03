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
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ProjectDetailPresenter.class)
public class ProjectDetailActivity extends BaseActivity<ProjectDetailPresenter> {
  protected @InjectView(R.id.backers_count) TextView backers_count;
  protected @InjectView(R.id.blurb) TextView blurb;
  protected @InjectView(R.id.category) TextView category;
  protected @InjectView(R.id.creator_name) TextView creator_name;
  protected @InjectView(R.id.deadline_countdown) TextView deadline_countdown;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadline_countdown_unit;
  protected @InjectView(R.id.goal) TextView goal;
  protected @InjectView(R.id.location) TextView location;
  protected @InjectView(R.id.name) TextView name;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentage_funded;
  protected @InjectView(R.id.project_detail_photo) ImageView photo;
  protected @InjectView(R.id.project_detail_video) VideoView video;
  protected @InjectView(R.id.play_button_overlay) ImageView play_button;
  protected @InjectView(R.id.pledged) TextView pledged;
  protected @InjectView(R.id.avatar) ImageView avatar;
  protected @InjectView(R.id.avatar_name) TextView avatar_name;
  protected @InjectView(R.id.fund_message) TextView fund_message;

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
  }

  public void show(final Project project) {
    backers_count.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.backersCount()));
    blurb.setText(project.blurb());
    creator_name.setText(project.creator().name());
    category.setText(project.category().name());
    deadline_countdown.setText(Integer.toString(project.deadlineCountdown()));
    deadline_countdown_unit.setText(project.deadlineCountdownUnit());
    goal.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    location.setText(project.location().displayableName());
    name.setText(project.name());
    percentage_funded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    pledged.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    if ( project.video() != null ) {
      loadVideo(project.video(), video);
    }
    else {
      Picasso.with(this).load(project.photo().full()).into(photo);
    }

    // Creator information
    Picasso.with(this).load(project.creator().avatar().medium()).into(avatar);
    avatar_name.setText(project.creator().name());
    fund_message.setText(String.format(getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    Timber.d("onBackPressed %s", this.toString());

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  public void loadVideo(Video video, VideoView videoView) {
    Picasso.with(this).load(video.frame()).into(photo); // todo: make this loading smoother
    Uri video_uri = Uri.parse(video.base());
    videoView.setVideoURI(video_uri);
    videoView.setMediaController(new MediaController(this));

    // todo: do we need a listener here?
    play_button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        photo.setVisibility(View.GONE);
        play_button.setVisibility(View.GONE);
        videoView.start();
      }
    });
  }

  public void backProjectButtonOnClick(final View v) {
    Timber.d("backProjectButtonOnClick");
    presenter.takeBackProjectClick();
  }


}
