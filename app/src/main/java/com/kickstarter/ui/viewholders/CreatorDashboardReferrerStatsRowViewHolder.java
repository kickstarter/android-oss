package com.kickstarter.ui.viewholders;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsRowHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardReferrerStatsRowViewHolder extends KSViewHolder {

  private final CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel viewModel;
  protected @Bind(R.id.amount_pledged_for_referrer_text_view) TextView amountPledgedForReferrerTextView;
  protected @Bind(R.id.percentage_pledged_for_referrer_text_view) TextView percentagePledgedForReferrerTextView;
  protected @Bind(R.id.referrer_source_text_view) TextView referrerSourceTextView;
  protected @Bind(R.id.referrer_backer_count_text_view) TextView referrerBackerCountTextView;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerStatsRowViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel.outputs.percentageOfTotalPledged()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percentagePledgedForReferrerTextView::setText);

    this.viewModel.outputs.projectAndPledgedForReferrer()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedColumnValue);

    this.viewModel.outputs.referrerBackerCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(referrerBackerCountTextView::setText);

    this.viewModel.outputs.referrerSourceName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(referrerSourceTextView::setText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats = requireNonNull((Pair<Project, ProjectStatsEnvelope.ReferrerStats>) data);
    this.viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats);
  }

  private void setPledgedColumnValue(final @NonNull Pair<Project, Float> projectAndPledgedForReferrer) {
    final String goalString = ksCurrency.format(projectAndPledgedForReferrer.second, projectAndPledgedForReferrer.first, false, true, RoundingMode.DOWN);
    amountPledgedForReferrerTextView.setText(goalString);
  }
}
