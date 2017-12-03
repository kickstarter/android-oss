package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class CreatorDashboardReferrerBreakdownViewHolder extends KSViewHolder {
  private final CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel viewModel;

  protected @BindView(R.id.average_pledge_amount_text_view) TextView averagePledgeAmountTextView;
  protected @BindView(R.id.amount_pledged_via_kickstarter_text_view) TextView amountPledgedViaInternalTextView;
  protected @BindView(R.id.amount_pledged_via_external_text_view) TextView amountPledgedViaExternalTextView;
  protected @BindView(R.id.amount_pledged_via_custom_text_view) TextView amountPledgedViaCustomTextView;
  protected @BindView(R.id.percent_via_custom_circle_text_view) ImageView percentCustomCircleTextView;
  protected @BindView(R.id.percent_via_custom_text_view) TextView percentCustomTextView;
  protected @BindView(R.id.percent_via_external_text_view) TextView percentExternalTextView;
  protected @BindView(R.id.percent_via_external_circle_text_view) ImageView percentExternalCircleTextView;
  protected @BindView(R.id.percent_via_kickstarter_text_view) TextView percentInternalTextView;
  protected @BindView(R.id.percent_via_kickstarter_circle_text_view) ImageView percentInternalCircleTextView;
  protected @BindView(R.id.pledged_via_custom_layout) LinearLayout pledgedViaCustomLayout;
  protected @BindView(R.id.pledged_via_external_layout) LinearLayout pledgedViaExternalLayout;
  protected @BindView(R.id.pledged_via_kickstarter_layout) LinearLayout pledgedViaInternalLayout;
  protected @BindView(R.id.referrer_breakdown_chart_layout) LinearLayout referrerBreakdownLayout;
  protected @BindView(R.id.referrer_breakdown_view) ReferrerBreakdownView referrerBreakdownView;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerBreakdownViewHolder(final @NonNull View view) {
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
      .subscribe(color -> DrawableCompat.setTint(this.percentCustomCircleTextView.getDrawable(), color));

    this.viewModel.outputs.customReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.referrerBreakdownView.setCustomAngleAndColor(percent * 360d));

    this.viewModel.outputs.customReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentCustomTextView::setText);

    this.viewModel.outputs.externalReferrerColor()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(color -> DrawableCompat.setTint(this.percentExternalCircleTextView.getDrawable(), color));

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
      .subscribe(color -> DrawableCompat.setTint(this.percentInternalCircleTextView.getDrawable(), color));

    this.viewModel.outputs.internalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> this.referrerBreakdownView.setInternalAngleAndColor(percent * 360d));

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

  private void setAmountPledgedTextViewText(final @NonNull Pair<Project, Float> projectAndAmount, final TextView textview) {
    final String amountString = this.ksCurrency.format(projectAndAmount.second, projectAndAmount.first, false, true, RoundingMode.DOWN);
    textview.setText(amountString);
  }

  private void setAveragePledgeTextViewText(final @NonNull Pair<Project, Integer> projectAndAveragePledge) {
    final String amountString = this.ksCurrency.format(projectAndAveragePledge.second, projectAndAveragePledge.first, true, true, RoundingMode.DOWN);
    this.averagePledgeAmountTextView.setText(amountString);
  }
}
