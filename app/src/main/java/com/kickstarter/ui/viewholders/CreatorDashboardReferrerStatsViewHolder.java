package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.adapters.CreatorDashboardReferrerStatsAdapter;
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsHolderViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardReferrerStatsViewHolder extends KSViewHolder {
  private final CreatorDashboardReferrerStatsHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.dashboard_referrer_stats_empty_text_view) TextView emptyTextView;
  protected @Bind(R.id.dashboard_referrer_stats_recycler_view) RecyclerView referrerStatsRecyclerView;
  protected @Bind(R.id.dashboard_referrer_title) TextView referrerTitleTextView;

  protected @BindString(R.string.Top_pledge_sources) String topPledgeSourcesString;
  protected @BindString(R.string.Top_ten_pledge_sources) String topTenPledgeSourcesString;

  public CreatorDashboardReferrerStatsViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardReferrerStatsHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    final CreatorDashboardReferrerStatsAdapter referrerStatsAdapter = new CreatorDashboardReferrerStatsAdapter();
    this.referrerStatsRecyclerView.setAdapter(referrerStatsAdapter);
    final LinearLayoutManager layoutManager = new LinearLayoutManager(context());
    this.referrerStatsRecyclerView.setLayoutManager(layoutManager);

    this.viewModel.outputs.projectAndReferrerStats()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(referrerStatsAdapter::takeProjectAndReferrerStats);

    this.viewModel.outputs.referrerStatsListIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::toggleRecyclerViewAndEmptyStateVisibility);

    this.viewModel.outputs.referrersTitleIsTopTen()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setTitleCopy);
  }

  private void setTitleCopy(final boolean shouldTitleHaveIsTen) {
    this.referrerTitleTextView.setText(shouldTitleHaveIsTen ? this.topTenPledgeSourcesString : this.topPledgeSourcesString);
  }

  private void toggleRecyclerViewAndEmptyStateVisibility(final @NonNull Boolean gone) {
    ViewUtils.setGone(this.referrerStatsRecyclerView, gone);
    ViewUtils.setGone(this.emptyTextView, !gone);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>> projectAndReferrerStats = requireNonNull((Pair<Project, List<ProjectStatsEnvelope.ReferrerStats>>) data);
    this.viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats);
  }

  @Override
  protected void destroy() {
    super.destroy();
    this.referrerStatsRecyclerView.setAdapter(null);
  }
}
