package com.kickstarter.ui.viewholders;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.RefTag;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.MessagesActivity;
import com.kickstarter.ui.activities.ProjectActivity;
import com.kickstarter.viewmodels.CreatorDashboardHeaderHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardHeaderViewHolder extends KSViewHolder {
  private final CreatorDashboardHeaderHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.creator_dashboard_amount_raised) TextView amountRaisedTextView;
  protected @Bind(R.id.creator_dashboard_backer_count) TextView backerCountTextView;
  protected @Bind(R.id.creator_dashboard_funding_text) TextView fundingTextTextView;
  protected @Bind(R.id.creator_dashboard_percent) TextView percentTextView;
  protected @Bind(R.id.creator_dashboard_project_blurb) TextView projectBlurbTextView;
  protected @Bind(R.id.creator_dashboard_project_name) TextView projectNameTextView;
  protected @Bind(R.id.creator_dashboard_time_remaining) TextView timeRemainingTextView;
  protected @Bind(R.id.creator_dashboard_time_remaining_text) TextView timeRemainingTextTextView;

  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  private final @Nullable Delegate delegate;

  private KSString ksString;
  private KSCurrency ksCurrency;

  public interface Delegate {
    void dashboardShowProjectMenuClicked();
  }

  public CreatorDashboardHeaderViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);

    this.viewModel = new CreatorDashboardHeaderHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);

    this.ksCurrency = this.environment().ksCurrency();
    this.ksString = this.environment().ksString();
    this.delegate = delegate;

    this.viewModel.outputs.currentProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setTimeRemainingTextTextView);

    this.viewModel.outputs.currentProject()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedOfGoalString);

    this.viewModel.outputs.percentageFunded()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentTextView::setText);

    this.viewModel.outputs.projectBackersCountText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backerCountTextView::setText);

    this.viewModel.outputs.projectBlurbTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectBlurbTextView::setText);

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.timeRemainingText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.timeRemainingTextView::setText);

    this.viewModel.outputs.startMessagesActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startMessagesActivity);

    this.viewModel.outputs.startProjectActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(projectAndRefTag -> this.startProjectActivity(projectAndRefTag.first, projectAndRefTag.second));
  }

  @OnClick(R.id.project_name_blurb_arrow_view)
  protected void dashboardShowProjectMenuClicked() {
    this.delegate.dashboardShowProjectMenuClicked();
  }

  @OnClick(R.id.creator_dashboard_messages_button)
  protected void dashbordMessagesButtonClick() {
    this.viewModel.inputs.messagesButtonClicked();
  }

  @OnClick(R.id.creator_view_project_button)
  protected void viewProjectButtonClicked() {
    this.viewModel.inputs.viewProjectButtonClicked();
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope> projectAndProjectStats = requireNonNull((Pair<Project, ProjectStatsEnvelope>) data);
    this.viewModel.inputs.projectAndStats(projectAndProjectStats);
  }

  private void setPledgedOfGoalString(final @NonNull Project currentProject) {
    final String goalString = this.ksCurrency.format(currentProject.pledged(), currentProject, false, true, RoundingMode.DOWN);
    this.amountRaisedTextView.setText(goalString);

    final String goalText = this.ksString.format(this.pledgedOfGoalString, "goal", goalString);
    this.fundingTextTextView.setText(goalText);
  }

  private void setTimeRemainingTextTextView(final @NonNull Project currentProject) {
    this.timeRemainingTextTextView.setText(ProjectUtils.deadlineCountdownDetail(currentProject, this.context(), this.ksString));
  }

  private void startMessagesActivity(final @NonNull Project project) {
    final Intent intent = new Intent(this.context(), MessagesActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    this.context().startActivity(intent);
  }

  private void startProjectActivity(final @NonNull Project project, final @NonNull RefTag refTag) {
    final Intent intent = new Intent(this.context(), ProjectActivity.class)
      .putExtra(IntentKey.PROJECT, project)
      .putExtra(IntentKey.REF_TAG, refTag);
    this.context().startActivity(intent);
  }
}
