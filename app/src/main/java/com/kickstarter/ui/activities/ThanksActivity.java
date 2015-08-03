package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RequiresPresenter;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.ThanksPresenter;
import com.kickstarter.ui.adapters.ProjectCardMiniAdapter;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

@RequiresPresenter(ThanksPresenter.class)
public class ThanksActivity extends BaseActivity<ThanksPresenter> {
  @InjectView(R.id.backed_project) TextView backedProject;
  @InjectView(R.id.recommended_projects_recycler_view) RecyclerView recommendedProjectsRecyclerView;

  ProjectCardMiniAdapter projectCardMiniAdapter;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.thanks_layout);
    ButterKnife.inject(this);

    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    recommendedProjectsRecyclerView.setLayoutManager(layoutManager);

    final Intent intent = getIntent();
    presenter.takeProject(intent.getExtras().getParcelable("project"));
  }

  public void show(final Project project) {
    // TODO: Bold project name
    backedProject.setText(getResources().getString(R.string.You_just_backed, project.name()));
  }

  public void showRecommendedProjects(final List<Project> projects) {
    projectCardMiniAdapter = new ProjectCardMiniAdapter(this, projects);
    recommendedProjectsRecyclerView.setAdapter(projectCardMiniAdapter);
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
