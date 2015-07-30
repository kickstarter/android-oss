package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ThanksPresenter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ThanksPresenter.class)
public class ThanksActivity extends BaseActivity<ThanksPresenter> {
  @InjectView(R.id.backed_project) TextView backedProject;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.thanks_layout);
    ButterKnife.inject(this);

    final Intent intent = getIntent();
    presenter.takeProject(intent.getExtras().getParcelable("project"));
  }

  public void show(final Project project) {
    // TODO: The project name should be in bold and dark text, but doing that in Android
    // is tricky.
    backedProject.setText(getResources().getString(R.string.You_just_backed, project.name()));
  }

  public void showRecommendedProjects(final List<Project> projects) {

  }

  public void onDoneClick(final View view) {
    Timber.d("onDoneClick");
    presenter.takeDoneClick();
  }

  public void onShareClick(final View view) {
    Timber.d("onShareClick");
    presenter.takeShareClick();
  }
}
