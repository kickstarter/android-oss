package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
  protected @Bind(R.id.pledged_via_custom_layout) FrameLayout pledgedViaCustomLayout;
  protected @Bind(R.id.pledged_via_external_layout) FrameLayout pledgedViaExternalLayout;
  protected @Bind(R.id.pledged_via_kickstarter_layout) FrameLayout pledgedViaInternalLayout;
  protected @Bind(R.id.referrer_breakdown_chart_layout) ConstraintLayout referrerBreakdownLayout;
  protected @Bind(R.id.referrer_breakdown_view) ReferrerBreakdownView referrerBreakdownView;

  private KSCurrency ksCurrency;

  public CreatorDashboardReferrerBreakdownViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
    this.ksCurrency = this.environment().ksCurrency();

    this.viewModel = new CreatorDashboardReferrerBreakdownHolderViewModel.ViewModel(environment());

//    this.viewModel.outputs.customReferrerColor()
//      .compose(bindToLifecycle())
//      .compose(observeForUI())
//      .subscribe(color -> DrawableCompat.setTint(this.percentCustomCircleTextView.getDrawable(), color));

    this.viewModel.outputs.customReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, pledgedViaCustomLayout));

    this.viewModel.outputs.customReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentCustomTextView::setText);

//    this.viewModel.outputs.externalReferrerColor()
//      .compose(bindToLifecycle())
//      .compose(observeForUI())
//      .subscribe(color -> DrawableCompat.setTint(this.percentExternalCircleTextView.getDrawable(), color));

    this.viewModel.outputs.externalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, pledgedViaExternalLayout));

    this.viewModel.outputs.externalReferrerPercentText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentExternalTextView::setText);

//    this.viewModel.outputs.internalReferrerColor()
//      .compose(bindToLifecycle())
//      .compose(observeForUI())
//      .subscribe(color -> DrawableCompat.setTint(this.percentInternalCircleTextView.getDrawable(), color));

    this.viewModel.outputs.internalReferrerPercent()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(percent -> setReferrerWidth(percent, pledgedViaInternalLayout));

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

  private static void setReferrerWidth(Float percent, FrameLayout frameLayout) {
    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) frameLayout.getLayoutParams();
    layoutParams.horizontalWeight = percent;
    frameLayout.setLayoutParams(layoutParams);
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
