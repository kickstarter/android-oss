package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.RecyclerViewPaginator;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.IntentUtils;
import com.kickstarter.libs.utils.ViewUtils;
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

import static com.kickstarter.libs.utils.IntegerUtils.isNonZero;

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
    final int spanCount = ViewUtils.isLandscape(this) ? 3 : 2;
    recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
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

    viewModel.outputs.showDiscovery()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(__ -> startDiscoveryActivity());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    paginator.stop();
    recyclerView.setAdapter(null);
  }

  private void loadProjects(final @NonNull List<Project> projects) {
    if (projects.size() == 0) {
      recyclerView.setLayoutManager(new LinearLayoutManager(this));
      recyclerView.setPadding(0, recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), recyclerView.getPaddingBottom());
      if (ViewUtils.isPortrait(this)) {
        recyclerView.setNestedScrollingEnabled(false);
      }
    }

    adapter.takeProjects(projects);
  }

  private void setViews(final @NonNull User user) {
    Picasso.with(this).load(user.avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    userNameTextView.setText(user.name());

    final Integer createdNum = user.createdProjectsCount();
    if (isNonZero(createdNum)) {
      createdNumTextView.setText(String.valueOf(createdNum));
    } else {
      createdTextView.setVisibility(View.GONE);
      createdNumTextView.setVisibility(View.GONE);
      dividerView.setVisibility(View.GONE);
    }

    final Integer backedNum = user.backedProjectsCount();
    if (isNonZero(backedNum)) {
      backedNumTextView.setText(String.valueOf(backedNum));
    } else {
      backedTextView.setVisibility(View.GONE);
      backedNumTextView.setVisibility(View.GONE);
      dividerView.setVisibility(View.GONE);
    }
  }

  private void startProjectActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this, ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    startActivityWithTransition(intent, R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  private void startDiscoveryActivity() {
    startActivity(IntentUtils.discoveryIntent(this));
  }
}
