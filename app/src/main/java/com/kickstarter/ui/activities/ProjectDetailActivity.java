package com.kickstarter.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ProjectDetailPresenter;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectDetailActivity extends Activity {
  static ProjectDetailPresenter presenter;
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
  protected @InjectView(R.id.photo) ImageView photo;
  protected @InjectView(R.id.pledged) TextView pledged;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.project_detail_layout);
    ButterKnife.inject(this);

    // TODO: What if the view is destroyed - will this be called again?
    // What happens to the intent?
    Intent intent = getIntent();
    Project project = intent.getExtras().getParcelable("project");

    if (presenter == null) {
      presenter = new ProjectDetailPresenter(project);
    }
    presenter.onTakeView(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    presenter.onTakeView(null);
    if (isFinishing())
      presenter = null;
  }

  public void show(Project project) {
    backers_count.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.backersCount()));
    blurb.setText(project.blurb());
    creator_name.setText(project.creator().name());
    category.setText(project.category().name());
    deadline_countdown.setText(Integer.toString(project.deadlineCountdown()));
    deadline_countdown_unit.setText(project.deadlineCountdownUnit());
    goal.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.goal()));
    location.setText(project.location().displayableName());
    name.setText(project.name());
    percentage_funded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(this).load(project.photo().full()).into(photo);
    pledged.setText(NumberFormat.getNumberInstance(Locale.getDefault()).format(project.pledged()));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
