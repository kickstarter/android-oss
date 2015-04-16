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
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ProjectDetailPresenter;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectDetailActivity extends Activity {
  static ProjectDetailPresenter presenter;
  protected @InjectView(R.id.photo) ImageView photo;
  protected @InjectView(R.id.category) TextView category;

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
    category.setText(project.category().name());
    Picasso.with(this).load(project.photo().full()).into(photo);
  }

  @Override
  public void onBackPressed() {
    Animation slideAnim = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
    slideAnim.setFillAfter(true);
    slideAnim.setAnimationListener(new Animation.AnimationListener() {
      public void onAnimationStart(Animation paramAnimation) { }
      public void onAnimationRepeat(Animation paramAnimation) { }
      public void onAnimationEnd(Animation paramAnimation) {
        finish();
        overridePendingTransition(0, 0);
      }
    });
    ViewGroup view = (ViewGroup) findViewById(android.R.id.content);
    view.startAnimation(slideAnim);
  }
}
