package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.adapters.ProfileAdapter;
import com.kickstarter.viewmodels.ProfileViewModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;

@RequiresViewModel(ProfileViewModel.class)
public final class ProfileActivity extends BaseActivity<ProfileViewModel> {
  private ProfileAdapter adapter;
  private RecyclerViewPaginator paginator;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.user_name) TextView userNameTextView;
  protected @Bind(R.id.created_num) TextView createdNumTextView;
  protected @Bind(R.id.backed_num) TextView backedNumTextView;
  protected @Bind(R.id.created) TextView createdTextView;
  protected @Bind(R.id.backed) TextView backedTextView;
  protected @Bind(R.id.divider) View dividerView;
  protected @Bind(R.id.recycler_view) RecyclerView recyclerView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);
    ButterKnife.bind(this);

    adapter = new ProfileAdapter(viewModel);
    recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    recyclerView.setAdapter(adapter);

    paginator = new RecyclerViewPaginator(recyclerView, viewModel.inputs::nextPage);

    viewModel.outputs.user()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::setViews);

    viewModel.outputs.projects()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::loadProjects);

    viewModel.outputs.showProject()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::startProjectActivity);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    paginator.stop();
  }

  private void loadProjects(final @NonNull List<Project> projects) {
    adapter.takeProjects(projects);
  }

  private void setViews(final @NonNull User user) {
    Picasso.with(this).load(user.avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    userNameTextView.setText(user.name());

    final Integer createdNum = user.createdProjectsCount();
    if (createdNum == null || createdNum == 0) {
      createdTextView.setVisibility(View.GONE);
      createdNumTextView.setVisibility(View.GONE);
      dividerView.setVisibility(View.GONE);
    } else {
      createdNumTextView.setText(createdNum.toString());
    }

    final Integer backedNum = user.backedProjectsCount();
    if (backedNum == null || backedNum == 0) {
      backedTextView.setVisibility(View.GONE);
      backedNumTextView.setVisibility(View.GONE);
      dividerView.setVisibility(View.GONE);
    } else {
      backedNumTextView.setText(backedNum.toString());
    }
  }

  private void startProjectActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }
}
