package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class ProjectCardViewHolder extends KSViewHolder {
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.pledged_of_) TextView pledgedOfTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Nullable @Bind(R.id.created_by) TextView createdByTextView;
  protected @Nullable @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.photo_gradient) ViewGroup photoGradientViewGroup;
  protected @Bind(R.id.potd_group) ViewGroup potdViewGroup;

  protected @BindString(R.string.backers) String backersString;
  protected @BindString(R.string.of_) String ofString;
  protected @BindString(R.string.pledged_of_) String pledgedOfString;
  protected @BindString(R.string._to_go) String toGoString;

  protected Project project;
  private final Delegate delegate;
  protected DiscoveryPresenter presenter;

  @Inject Money money;

  public interface Delegate {
    void projectCardClick(ProjectCardViewHolder viewHolder, Project project);
  }

  public ProjectCardViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    this.project = (Project) datum;

    backersCountTextView.setText(project.formattedBackersCount());
    categoryTextView.setText(project.category().name());
    deadlineCountdownTextView.setText(Integer.toString(project.deadlineCountdownValue()));
    deadlineCountdownUnitTextView.setText(project.deadlineCountdownUnit(view.getContext()));
    goalTextView.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    locationTextView.setText(project.location().displayableName());
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    nameTextView.setText(project.name());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(view.getContext()).
      load(project.photo().full()).
      into(photoImageView);

    final int potdVisible = project.isPotdToday() ? View.VISIBLE : View.INVISIBLE;
    photoGradientViewGroup.setVisibility(potdVisible);
    potdViewGroup.setVisibility(potdVisible);

    /* a11y */
    if (ViewUtils.isFontScaleLarge(view.getContext())) {
      pledgedOfTextView.setText(ofString);
    } else {
      pledgedOfTextView.setText(pledgedOfString);
    }

    setStatsContentDescription();

    /* landscape-specific */
    if (createdByTextView != null) {
      createdByTextView.setText(String.format(view.getContext().getString(R.string.by_), project.creator().name()));
    }

    if (blurbTextView != null) {
      blurbTextView.setText(project.blurb());
    }
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectCardClick(this, project);
  }

  public void setStatsContentDescription() {
    final String backersCountContentDescription = project.formattedBackersCount() + backersString;
    final String pledgedContentDescription = String.valueOf(project.pledged()) + pledgedOfTextView.getText() +
      money.formattedCurrency(project.goal(), project.currencyOptions());
    final String deadlineCountdownContentDescription = project.deadlineCountdownValue() +
      project.deadlineCountdownUnit(view.getContext()) + toGoString;

    backersCountTextView.setContentDescription(backersCountContentDescription);
    pledgedTextView.setContentDescription(pledgedContentDescription);
    deadlineCountdownTextView.setContentDescription(deadlineCountdownContentDescription);
  }
}
