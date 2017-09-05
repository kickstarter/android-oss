package com.kickstarter.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.libs.BaseFragment;
import com.kickstarter.libs.qualifiers.RequiresFragmentViewModel;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.ArgumentsKey;
import com.kickstarter.ui.activities.CreatorDashboardActivity;
import com.kickstarter.ui.adapters.CreatorDashboardAdapter;
import com.kickstarter.viewmodels.CreatorDashboardFragmentViewModel;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;

@RequiresFragmentViewModel(CreatorDashboardFragmentViewModel.ViewModel.class)
public final class CreatorDashboardFragment extends BaseFragment<CreatorDashboardFragmentViewModel.ViewModel> {
  private CreatorDashboardAdapter adapter;
  private RecyclerView creatorDashboardRecyclerView;

  public CreatorDashboardFragment() {}

  public static @NonNull CreatorDashboardFragment newInstance(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
    final CreatorDashboardFragment fragment = new CreatorDashboardFragment();
    final Bundle bundle = new Bundle();
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT, projectAndStats.first);
    bundle.putParcelable(ArgumentsKey.CREATOR_DASHBOARD_PROJECT_STATS, projectAndStats.second);
    fragment.setArguments(bundle);
    return fragment;
  }

  @Override
  public @Nullable View onCreateView(final @NonNull LayoutInflater inflater, final @Nullable ViewGroup container, final @Nullable Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    this.creatorDashboardRecyclerView = (RecyclerView) inflater.inflate(R.layout.creator_dashboard_recycler_layout, container, false);

    this.adapter = new CreatorDashboardAdapter(this.viewModel.inputs);
    this.creatorDashboardRecyclerView.setAdapter(this.adapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(this.creatorDashboardRecyclerView.getContext());
    this.creatorDashboardRecyclerView.setLayoutManager(layoutManager);

    this.viewModel.outputs.projectAndStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::renderProjectAndStats);

    this.viewModel.outputs.toggleBottomSheet()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ ->  {
        final CreatorDashboardActivity activity = (CreatorDashboardActivity) getActivity();
        activity.toggleBottomSheetClick();
      });

    return this.creatorDashboardRecyclerView;
  }

  private void renderProjectAndStats(final @NonNull Pair<Project, ProjectStatsEnvelope> projectAndStats) {
    this.adapter.takeProjectAndStats(projectAndStats);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    this.creatorDashboardRecyclerView.setAdapter(null);
  }
}
