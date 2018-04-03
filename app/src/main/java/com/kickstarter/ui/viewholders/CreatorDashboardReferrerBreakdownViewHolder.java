package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.viewmodels.CreatorDashboardReferrerBreakdownHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardReferrerBreakdownViewHolder extends KSViewHolder {
  private final CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.amount_pledged_via_custom_text_view) TextView amountPledgedViaCustomTextView;
  protected @Bind(R.id.amount_pledged_via_external_text_view) TextView amountPledgedViaExternalTextView;
  protected @Bind(R.id.amount_pledged_via_kickstarter_text_view) TextView amountPledgedViaKickstarterTextView;
  protected @Bind(R.id.dashboard_referrer_breakdown_empty_text_view) TextView emptyCopyTextView;
  protected @Bind(R.id.percent_via_custom_text_view) TextView percentCustomTextView;
  protected @Bind(R.id.percent_via_external_text_view) TextView percentExternalTextView;
  protected @Bind(R.id.percent_via_kickstarter_text_view) TextView percentKickstarterTextView;
  protected @Bind(R.id.pledged_via_custom) View pledgedViaCustomLayout;
  protected @Bind(R.id.pledged_via_custom_bar) View pledgedViaCustomBar;
  protected @Bind(R.id.pledged_via_custom_indicator) View pledgedViaCustomIndicator;
  protected @Bind(R.id.pledged_via_external) View pledgedViaExternalLayout;
  protected @Bind(R.id.pledged_via_external_bar) View pledgedViaExternalBar;
  protected @Bind(R.id.pledged_via_external_indicator) View pledgedViaExternalIndicator;
  protected @Bind(R.id.pledged_via_kickstarter_bar) View pledgedViaKickstarterBar;
  protected @Bind(R.id.pledged_via_kickstarter_indicator) View pledgedViaKickstarterIndicator;
  protected @Bind(R.id.pledged_via_kickstarter) View pledgedViaKickstarterLayout;
  protected @Bind(R.id.referrer_breakdown_chart_layout) ConstraintLayout referrerBreakdownLayout;

  protected @BindDimen(R.dimen.grid_1) int grid1Pixels;
  protected @BindDimen(R.dimen.grid_3) int grid3Pixels;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerBreakdownViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment());

    this.viewModel.outputs.breakdownViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.referrerBreakdownLayout));

    this.viewModel.outputs.emptyViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.emptyCopyTextView));

    this.viewModel.outputs.customReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, this.pledgedViaCustomBar, this.pledgedViaCustomIndicator));

    this.viewModel.outputs.customReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentCustomTextView::setText);

    this.viewModel.outputs.externalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, this.pledgedViaExternalBar, this.pledgedViaExternalIndicator));

    this.viewModel.outputs.externalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> flipIndicatorIfStatsOffScreen(this.pledgedViaExternalIndicator, this.pledgedViaExternalLayout));

    this.viewModel.outputs.externalReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentExternalTextView::setText);

    this.viewModel.outputs.kickstarterReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, this.pledgedViaKickstarterBar, this.pledgedViaKickstarterIndicator));

    this.viewModel.outputs.kickstarterReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentKickstarterTextView::setText);

    this.viewModel.outputs.pledgedViaCustomLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaCustomLayout, this.pledgedViaCustomBar, this.pledgedViaCustomIndicator));

    this.viewModel.outputs.pledgedViaExternalLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaExternalLayout, this.pledgedViaExternalBar, this.pledgedViaExternalIndicator));

    this.viewModel.outputs.pledgedViaKickstarterLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaKickstarterLayout, this.pledgedViaKickstarterBar, this.pledgedViaKickstarterIndicator));

    this.viewModel.outputs.projectAndCustomReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaCustomTextView));

    this.viewModel.outputs.projectAndExternalReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaExternalTextView));

    this.viewModel.outputs.projectAndKickstarterReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaKickstarterTextView));
  }

  private void setReferrerWidth(final Float percent, final View bar, final View indicator) {
    final ConstraintLayout.LayoutParams barLayoutParams = (ConstraintLayout.LayoutParams) bar.getLayoutParams();
    barLayoutParams.horizontalWeight = percent;
    bar.setLayoutParams(barLayoutParams);

    adjustIndicatorMarginForShortBar(bar, indicator);
  }

  private void adjustIndicatorMarginForShortBar(final View bar, final View indicator) {
    bar.post(() -> {
      if (bar.getMeasuredWidth() < this.grid3Pixels) {
        final ConstraintLayout.LayoutParams indicatorLayoutParams = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        indicatorLayoutParams.startToStart = bar.getId();
        indicatorLayoutParams.endToEnd = bar.getId();
        indicator.setLayoutParams(indicatorLayoutParams);
      }
    });
  }

  private void flipIndicatorIfStatsOffScreen(final View indicator, final View stats) {
    stats.post(() -> {
      final int leftVisibleEdgeOfBreakdownView = this.referrerBreakdownLayout.getLeft() + this.referrerBreakdownLayout.getPaddingLeft();
      if (stats.getLeft() < leftVisibleEdgeOfBreakdownView) {
        indicator.setScaleX(-1);
        final ConstraintLayout.LayoutParams indicatorLayoutParams = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        indicatorLayoutParams.setMarginStart(this.grid3Pixels);
        indicator.setLayoutParams(indicatorLayoutParams);

        final ConstraintLayout.LayoutParams statsLayoutParams = (ConstraintLayout.LayoutParams) stats.getLayoutParams();
        statsLayoutParams.startToEnd = indicator.getId();
        statsLayoutParams.setMarginStart(this.grid1Pixels);
        statsLayoutParams.endToStart = ConstraintLayout.LayoutParams.UNSET;
        stats.setLayoutParams(statsLayoutParams);
      }
    });
  }

  private void hideReferrer(final boolean gone, final @NonNull View layout, final @NonNull View bar, final @NonNull View indicator) {
    ViewUtils.setGone(layout, gone);
    ViewUtils.setGone(bar, gone);
    ViewUtils.setGone(indicator, gone);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope> projectAndStats = requireNonNull((Pair<Project, ProjectStatsEnvelope>) data);
    this.viewModel.inputs.projectAndStatsInput(projectAndStats);
  }

  private void setAmountPledgedTextViewText(final @NonNull Pair<Project, Float> projectAndAmount, final TextView textview) {
    final String amountString = this.ksCurrency.format(projectAndAmount.second, projectAndAmount.first, false, true, RoundingMode.DOWN);
    textview.setText(amountString);
  }
}
