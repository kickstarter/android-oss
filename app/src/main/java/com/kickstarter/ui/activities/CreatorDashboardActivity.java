package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ToolbarUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;
import com.kickstarter.ui.fragments.CreatorDashboardFragment;
import com.kickstarter.viewmodels.CreatorDashboardViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel.class)
public final class CreatorDashboardActivity extends BaseActivity<CreatorDashboardViewModel.ViewModel> {

  private CreatorDashboardBottomSheetAdapter bottomSheetAdapter;
  private BottomSheetBehavior bottomSheetBehavior;

  protected @Bind(R.id.creator_dashboard_app_bar) AppBarLayout appBarLayout;
  protected @Bind(R.id.creator_dashboard_project_name_small) TextView collapsedToolbarTitle;
  protected @Bind(R.id.creator_dashboard_bottom_sheet_recycler_view) RecyclerView bottomSheetRecyclerView;
  protected @Bind(R.id.creator_dashboard_bottom_sheet_scrim) View bottomSheetScrim;
  protected @Bind(R.id.creator_dashboard_project_name) TextView projectNameTextView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.creator_dashboard_layout);
    ButterKnife.bind(this);

    // Set up the bottom sheet recycler view.
    this.bottomSheetAdapter = new CreatorDashboardBottomSheetAdapter(this.viewModel.inputs);
    this.bottomSheetRecyclerView.setAdapter(this.bottomSheetAdapter);
    this.bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    this.bottomSheetBehavior = BottomSheetBehavior.from(this.bottomSheetRecyclerView);

    ToolbarUtils.INSTANCE.fadeAndTranslateToolbarTitleOnExpand(this.appBarLayout, this.collapsedToolbarTitle);

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::createProjectDashboardFragment);

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pair -> this.projectNameTextView.setText(pair.first.name()));

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pair -> this.collapsedToolbarTitle.setText(pair.first.name()));

    this.viewModel.outputs.projectsForBottomSheet()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectsForDropdown);

    this.viewModel.outputs.projectAndStats()
       .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> newProjectClicked());

    createAndSetBottomSheetCallback();
  }

  @Override
  public void back() {
    if (this.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
      hideBottomSheet();
    } else {
      super.back();
    }
  }

  @OnClick(R.id.creator_dashboard_bottom_sheet_scrim)
  protected void bottomSheetScrimClicked() {
    hideBottomSheet();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.bottomSheetRecyclerView.setAdapter(null);
    this.bottomSheetBehavior.setBottomSheetCallback(null);
  }

  private void createAndSetBottomSheetCallback() {
    final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(final @NonNull View bottomSheet, final int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
          CreatorDashboardActivity.this.bottomSheetScrim.setVisibility(View.GONE);
        }
      }
      @Override
      public void onSlide(final @NonNull View bottomSheet, final float slideOffset) {

      }
    };

    this.bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
  }

  private void setProjectsForDropdown(final @NonNull List<Project> projects) {
    this.bottomSheetAdapter.takeProjects(projects);
  }

  private void newProjectClicked() {
    hideBottomSheet();
  }

  private void hideBottomSheet() {
    this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    this.bottomSheetScrim.setVisibility(View.GONE);
  }

  private void createProjectDashboardFragment(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    final CreatorDashboardFragment fragment = CreatorDashboardFragment.newInstance(projectAndStats);
    fragmentTransaction.replace(R.id.creator_dashboard_coordinator_view, fragment);
    fragmentTransaction.commit();
  }

  public void toggleBottomSheetClick() {
    this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    this.bottomSheetScrim.setVisibility(View.VISIBLE);
  }
}
