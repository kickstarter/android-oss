package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.models.Project;
import com.kickstarter.ui.adapters.ProjectSocialAdapter;
import com.kickstarter.ui.viewholders.ProjectContextViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectSocialActivity extends BaseActivity implements ProjectSocialAdapter.Delegate {
  protected @Bind(R.id.project_social_recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.project_social_layout);
    ButterKnife.bind(this);

    final Intent intent = getIntent();
    final Project project = intent.getParcelableExtra(getString(R.string.intent_project));
    final ProjectSocialAdapter adapter = new ProjectSocialAdapter(this);

    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter.takeProject(project);
  }

  @Override
  @OnClick(R.id.back_button)
  public void onBackPressed() {
    super.onBackPressed();
    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }

  @Override
  public void projectContextClicked(final @NonNull ProjectContextViewHolder viewHolder) {
    onBackPressed();
  }
}
