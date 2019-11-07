package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.qualifiers.RequiresActivityViewModel;
import com.kickstarter.libs.utils.ToolbarUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.ui.adapters.CreatorDashboardBottomSheetAdapter;
import com.kickstarter.viewmodels.CreatorDashboardViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresActivityViewModel(CreatorDashboardViewModel.ViewModel.class)
public final class CreatorDashboardActivity extends BaseActivity<CreatorDashboardViewModel.ViewModel> {

  private CreatorDashboardBottomSheetAdapter bottomSheetAdapter;
  private CreatorDashboardAdapter adapter;
  private BottomSheetBehavior bottomSheetBehavior;

  protected @Bind(R.id.creator_dashboard_app_bar) AppBarLayout appBarLayout;
  protected @Bind(R.id.creator_dashboard_project_name_small) TextView collapsedToolbarTitle;
  protected @Bind(R.id.creator_dashboard_recycler_view) RecyclerView recyclerView;
  protected @Bind(R.id.creator_dashboard_bottom_sheet_recycler_view) RecyclerView bottomSheetRecyclerView;
  protected @Bind(R.id.creator_dashboard_bottom_sheet_scrim) View bottomSheetScrim;
  protected @Bind(R.id.creator_dashboard_progress_bar) ProgressBar progressBar;
  protected @Bind(R.id.creator_dashboard_project_name) TextView projectNameTextView;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.creator_dashboard_layout);
    ButterKnife.bind(this);

    this.adapter = new CreatorDashboardAdapter(this.viewModel.inputs);
    this.recyclerView.setAdapter(this.adapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    this.recyclerView.setLayoutManager(layoutManager);

    // Set up the bottom sheet recycler view.
    this.bottomSheetAdapter = new CreatorDashboardBottomSheetAdapter(this.viewModel.inputs);
    this.bottomSheetRecyclerView.setAdapter(this.bottomSheetAdapter);
    this.bottomSheetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    this.bottomSheetBehavior = BottomSheetBehavior.from(this.bottomSheetRecyclerView);

    ToolbarUtils.INSTANCE.fadeAndTranslateToolbarTitleOnExpand(this.appBarLayout, this.collapsedToolbarTitle);

    this.viewModel.outputs.bottomSheetShouldExpand()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::bottomSheetShouldExpand);

    this.viewModel.outputs.progressBarIsVisible()
      .compose(bindToLifecycle())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(visible -> ViewUtils.setGone(this.progressBar, !visible));

    this.viewModel.outputs.projectDashboardData()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.adapter::takeProjectDashboardData);

    this.viewModel.outputs.projectName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectNameTextViews);

    this.viewModel.outputs.projectsForBottomSheet()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectsForDropdown);

    createAndSetBottomSheetCallback();
  }

  @Override
  public void back() {
    if (this.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
      this.viewModel.inputs.backClicked();
    } else {
      super.back();
    }
  }

  @OnClick(R.id.creator_dashboard_bottom_sheet_scrim)
  protected void bottomSheetScrimClicked() {
    this.viewModel.inputs.scrimClicked();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.recyclerView.setAdapter(null);
    this.bottomSheetRecyclerView.setAdapter(null);
    this.bottomSheetBehavior.setBottomSheetCallback(null);
  }

  public void bottomSheetShouldExpand(final boolean expand) {
    if(expand) {
      showBottomSheet();
    } else {
      hideBottomSheet();
    }
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

  private void setProjectNameTextViews(final @NonNull String projectName) {
    this.projectNameTextView.setText(projectName);
    this.collapsedToolbarTitle.setText(projectName);
  }

  private void hideBottomSheet() {
    this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    this.bottomSheetScrim.setVisibility(View.GONE);
  }

  public void showBottomSheet() {
    this.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    this.bottomSheetScrim.setVisibility(View.VISIBLE);
  }
}
