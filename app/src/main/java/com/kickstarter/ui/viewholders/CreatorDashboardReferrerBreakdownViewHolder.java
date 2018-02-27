package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
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

  protected @Bind(R.id.amount_pledged_via_kickstarter_text_view) TextView amountPledgedViaInternalTextView;
  protected @Bind(R.id.amount_pledged_via_external_text_view) TextView amountPledgedViaExternalTextView;
  protected @Bind(R.id.amount_pledged_via_custom_text_view) TextView amountPledgedViaCustomTextView;
  protected @Bind(R.id.percent_via_custom_circle_text_view) ImageView percentCustomCircleTextView;
  protected @Bind(R.id.percent_via_custom_text_view) TextView percentCustomTextView;
  protected @Bind(R.id.percent_via_external_text_view) TextView percentExternalTextView;
  protected @Bind(R.id.percent_via_external_circle_text_view) ImageView percentExternalCircleTextView;
  protected @Bind(R.id.percent_via_kickstarter_text_view) TextView percentInternalTextView;
  protected @Bind(R.id.percent_via_kickstarter_circle_text_view) ImageView percentInternalCircleTextView;
  protected @Bind(R.id.pledged_via_custom) View pledgedViaCustomLayout;
  protected @Bind(R.id.pledged_via_custom_bar) View pledgedViaCustomBar;
  protected @Bind(R.id.pledged_via_custom_indicator) View pledgedViaCustomIndicator;
  protected @Bind(R.id.pledged_via_external) View pledgedViaExternalLayout;
  protected @Bind(R.id.pledged_via_external_bar) View pledgedViaExternalBar;
  protected @Bind(R.id.pledged_via_external_indicator) View pledgedViaExternalIndicator;
  protected @Bind(R.id.pledged_via_kickstarter_bar) View pledgedViaInternalBar;
  protected @Bind(R.id.pledged_via_kickstarter_indicator) View pledgedViaInternalIndicator;
  protected @Bind(R.id.pledged_via_kickstarter) View pledgedViaInternalLayout;
  protected @Bind(R.id.referrer_breakdown_chart_layout) ConstraintLayout referrerBreakdownLayout;

  protected @BindDimen(R.dimen.grid_1_half) int grid1HalfPixels;
  protected @BindDimen(R.dimen.grid_1) int grid1Pixels;
  protected @BindDimen(R.dimen.grid_3) int grid3Pixels;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerBreakdownViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment());

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

    this.viewModel.outputs.internalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, this.pledgedViaInternalBar, this.pledgedViaInternalIndicator));

    this.viewModel.outputs.internalReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentInternalTextView::setText);

    this.viewModel.outputs.pledgedViaCustomLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaCustomLayout, this.pledgedViaCustomBar, this.pledgedViaCustomIndicator));

    this.viewModel.outputs.pledgedViaExternalLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaExternalLayout, this.pledgedViaExternalBar, this.pledgedViaExternalIndicator));

    this.viewModel.outputs.pledgedViaInternalLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(gone -> this.hideReferrer(gone, this.pledgedViaInternalLayout, this.pledgedViaInternalBar, this.pledgedViaInternalIndicator));

    this.viewModel.outputs.projectAndCustomReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaCustomTextView));

    this.viewModel.outputs.projectAndExternalReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaExternalTextView));

    this.viewModel.outputs.projectAndInternalReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(pa -> this.setAmountPledgedTextViewText(pa, this.amountPledgedViaInternalTextView));
  }

  private void setReferrerWidth(Float percent, View bar, View indicator) {
    ConstraintLayout.LayoutParams barLayoutParams = (ConstraintLayout.LayoutParams) bar.getLayoutParams();
    barLayoutParams.horizontalWeight = percent;
    bar.setLayoutParams(barLayoutParams);

    adjustIndicatorMarginForShortBar(bar, indicator);
  }

  // i don't prefer to do this and am ~open~ to suggestions
  private void adjustIndicatorMarginForShortBar(View bar, View indicator) {
    bar.post(() -> {
      if (bar.getMeasuredWidth() < grid3Pixels) {
        ConstraintLayout.LayoutParams indicatorLayoutParams = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        indicatorLayoutParams.startToStart = bar.getId();
        indicatorLayoutParams.endToEnd = bar.getId();
        indicator.setLayoutParams(indicatorLayoutParams);
      }
    });
  }

  //same here
  private void flipIndicatorIfStatsOffScreen(View indicator, View stats) {
    stats.post(() -> {
      if (stats.getLeft() < referrerBreakdownLayout.getLeft()) {
        indicator.setScaleX(-1);
        ConstraintLayout.LayoutParams indicatorLayoutParams = (ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        indicatorLayoutParams.setMarginStart(grid3Pixels);
        indicator.setLayoutParams(indicatorLayoutParams);

        ConstraintLayout.LayoutParams statsLayoutParams = (ConstraintLayout.LayoutParams) stats.getLayoutParams();
        statsLayoutParams.startToEnd = indicator.getId();
        statsLayoutParams.setMarginStart(grid1Pixels);
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
