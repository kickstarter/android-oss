package com.kickstarter.ui.viewholders;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.models.ProjectStats;
import com.kickstarter.viewmodels.CreatorDashboardHeaderHolderViewModel;

import butterknife.Bind;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardHeaderViewHolder extends KSViewHolder {

  private final CreatorDashboardHeaderHolderViewModel viewModel;

  protected @Bind(R.id.creator_dashboard_time_remaining_text) TextView timeRemainingTextTextView;

  private KSString ksString;

  public CreatorDashboardHeaderViewHolder(final @NonNull View view) {
    super(view);

    viewModel = new CreatorDashboardHeaderHolderViewModel(environment());
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    // coerce to projectstats and project
    final Pair<Project, ProjectStats> projectAndProjectStats = requireNonNull((Pair<Project, ProjectStats>) data);
//    viewmodel.inputs.project
  }

  private void setTimeRemainingTextTextView(final @NonNull Project latestProject) {
    timeRemainingTextTextView.setText(ProjectUtils.deadlineCountdownDetail(latestProject, this.context(), ksString));
  }
}
