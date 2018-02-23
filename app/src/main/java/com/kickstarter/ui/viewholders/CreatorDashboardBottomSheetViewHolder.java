package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.CreatorDashboardBottomSheetHolderViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardBottomSheetViewHolder extends KSViewHolder {
  private final CreatorDashboardBottomSheetHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.creator_dashboard_project_switcher_project_title) TextView projectNameTextView;
  protected @Bind(R.id.creator_dashboard_project_switcher_project_launch) TextView projectLaunchDateTextView;

  public interface Delegate {
    void projectSelectionInput(Project project);
  }

  public CreatorDashboardBottomSheetViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.viewModel = new CreatorDashboardBottomSheetHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    this.viewModel.outputs.projectNameText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.projectLaunchDate()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(d -> this.projectLaunchDateTextView.setText(DateTimeUtils.longDate(d)));

    this.viewModel.outputs.projectSwitcherProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(delegate::projectSelectionInput);
  }

  @OnClick(R.id.creator_dashboard_bottom_sheet_project_view)
  public void projectSwitcherProjectClicked() {
    this.viewModel.inputs.projectSwitcherProjectClicked();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Project project = requireNonNull((Project) data);
    this.viewModel.inputs.projectInput(project);
  }
}
