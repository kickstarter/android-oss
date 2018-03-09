package com.kickstarter.ui.viewholders;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.viewmodels.CreatorDashboardReferrerStatsRowHolderViewModel;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class CreatorDashboardReferrerStatsRowViewHolder extends KSViewHolder {

  private final CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel viewModel;
  protected @Bind(R.id.amount_pledged_for_referrer_text_view) TextView amountPledgedForReferrerTextView;
  protected @Bind(R.id.referrer_source_circle) ImageView referrerSourceCircleImageView;
  protected @Bind(R.id.referrer_source_text_view) TextView referrerSourceTextView;
  protected @Bind(R.id.referrer_backer_count_text_view) TextView referrerBackerCountTextView;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerStatsRowViewHolder(final @NonNull View view) {
    super(view);
    this.viewModel = new CreatorDashboardReferrerStatsRowHolderViewModel.ViewModel(environment());
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel.outputs.projectAndPledgedForReferrer()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setPledgedColumnValue);

    this.viewModel.outputs.referrerBackerCount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.referrerBackerCountTextView::setText);

    this.viewModel.outputs.referrerSourceColorId()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setReferrerCircleColor);

    this.viewModel.outputs.referrerSourceName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.referrerSourceTextView::setText);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope.ReferrerStats> projectAndReferrerStats = requireNonNull((Pair<Project, ProjectStatsEnvelope.ReferrerStats>) data);
    this.viewModel.inputs.projectAndReferrerStatsInput(projectAndReferrerStats);
  }

  private void setPledgedColumnValue(final @NonNull Pair<Project, Float> projectAndPledgedForReferrer) {
    final String goalString = this.ksCurrency.format(projectAndPledgedForReferrer.second, projectAndPledgedForReferrer.first);
    this.amountPledgedForReferrerTextView.setText(goalString);
  }

  private void setReferrerCircleColor(final @NonNull Integer referrerCircleColorResourceId) {
    final Integer color = ContextCompat.getColor(context(), referrerCircleColorResourceId);
    DrawableCompat.setTint(this.referrerSourceCircleImageView.getDrawable(), color);
  }
}
