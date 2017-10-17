package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.apiresponses.ProjectStatsEnvelope;
import com.kickstarter.ui.views.ReferrerBreakdownView;
import com.kickstarter.viewmodels.CreatorDashboardReferrerBreakdownHolderViewModel;

import java.math.RoundingMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardReferrerBreakDownViewHolder extends KSViewHolder {
  private final CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel viewModel;

  protected @Bind(R.id.average_pledge_amount_text_view) TextView averagePledgeAmountTextView;
  protected @Bind(R.id.referrer_breakdown_chart_layout) LinearLayout referrerBreakdownLayout;
  protected @Bind(R.id.amount_pledged_via_kickstarter_text_view) TextView amountPledgedViaInternalTextView;
  protected @Bind(R.id.amount_pledged_via_external_text_view) TextView amountPledgedViaExternalTextView;
  protected @Bind(R.id.amount_pledged_via_custom_text_view) TextView amountPledgedViaCustomTextView;
  protected @Bind(R.id.percent_via_custom_circle_text_view) TextView percentCustomCircleTextView;
  protected @Bind(R.id.percent_via_custom_text_view) TextView percentCustomTextView;
  protected @Bind(R.id.percent_via_external_text_view) TextView percentExternalTextView;
  protected @Bind(R.id.percent_via_external_circle_text_view) TextView percentExternalCircleTextView;
  protected @Bind(R.id.percent_via_kickstarter_text_view) TextView percentInternalTextView;
  protected @Bind(R.id.percent_via_kickstarter_circle_text_view) TextView percentInternalCircleTextView;
  protected @Bind(R.id.pledged_via_custom_layout) LinearLayout pledgedViaCustomLayout;
  protected @Bind(R.id.pledged_via_external_layout) LinearLayout pledgedViaExternalLayout;
  protected @Bind(R.id.pledged_via_kickstarter_layout) LinearLayout pledgedViaInternalLayout;

  @Bind(R.id.referrer_breakdown_view) ReferrerBreakdownView referrerBreakdownView;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerBreakDownViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment());

    this.viewModel.outputs.projectAndAveragePledge()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setAveragePledgeTextViewText);

    this.viewModel.outputs.customReferrerColor()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentCustomCircleTextView::setTextColor);

    this.viewModel.outputs.customReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.referrerBreakdownView.setCustomAngleAndColor(percent * 360d));

    this.viewModel.outputs.customReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentCustomTextView::setText);

    this.viewModel.outputs.customReferrerPledgedAmount()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(amount -> this.amountPledgedViaCustomTextView.setText(String.valueOf(amount)));

    this.viewModel.outputs.externalReferrerColor()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentExternalCircleTextView::setTextColor);

    this.viewModel.outputs.externalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.referrerBreakdownView.setExternalAngleAndColor(percent * 360d));

    this.viewModel.outputs.externalReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentExternalTextView::setText);

    this.viewModel.outputs.internalReferrerColor()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentInternalCircleTextView::setTextColor);

    this.viewModel.outputs.internalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.referrerBreakdownView.setInternalAngleAndColor(percent * 360d));

    this.viewModel.outputs.internalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.percentInternalTextView.setText(String.valueOf(percent * 100d)));

    this.viewModel.outputs.internalReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentInternalTextView::setText);

    this.viewModel.outputs.pledgedViaCustomLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.pledgedViaCustomLayout));

    this.viewModel.outputs.pledgedViaExternalLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.pledgedViaExternalLayout));

    this.viewModel.outputs.pledgedViaInternalLayoutIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.pledgedViaInternalLayout));

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

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, ProjectStatsEnvelope> projectAndStats = requireNonNull((Pair<Project, ProjectStatsEnvelope>) data);
    this.viewModel.inputs.projectAndStatsInput(projectAndStats);
  }

  private void setAmountPledgedTextViewText(final Pair<Project, Float> projectAndAmount, final TextView textview) {
    final String amountString = this.ksCurrency.format(projectAndAmount.second, projectAndAmount.first, false, true, RoundingMode.DOWN);
    textview.setText(amountString);
  }

  private void setAveragePledgeTextViewText(final @NonNull Pair<Project, Integer> projectAndAveragePledge) {
    final String amountString = this.ksCurrency.format(projectAndAveragePledge.second, projectAndAveragePledge.first, true, true, RoundingMode.DOWN);
    this.averagePledgeAmountTextView.setText(amountString);
  }
}
