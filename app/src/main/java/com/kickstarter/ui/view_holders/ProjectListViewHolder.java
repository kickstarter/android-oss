package com.kickstarter.ui.view_holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.models.Project;
import com.kickstarter.presenters.DiscoveryPresenter;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectListViewHolder extends RecyclerView.ViewHolder {
  protected @InjectView(R.id.backers_count) TextView backersCountTextView;
  protected @InjectView(R.id.category) TextView categoryTextView;
  protected @InjectView(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @InjectView(R.id.goal) TextView goalTextView;
  protected @InjectView(R.id.location) TextView locationTextView;
  protected @InjectView(R.id.name) TextView nameTextView;
  protected @InjectView(R.id.pledged) TextView pledgedTextView;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @InjectView(R.id.photo) ImageView photoImageView;
  protected @InjectView(R.id.photo_gradient) ViewGroup photoGradientViewGroup;
  protected @InjectView(R.id.potd_group) ViewGroup potdViewGroup;
  protected View view;
  protected Project project;
  protected DiscoveryPresenter presenter;
  @Inject Money money;

  public ProjectListViewHolder(View view, DiscoveryPresenter presenter) {
    super(view);
    this.view = view;
    this.presenter = presenter;

    ((KsrApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.inject(this, view);

    view.setOnClickListener((final View v) -> presenter.takeProjectClick(project));
  }

  public void onBind(final Project project) {
    this.project = project;

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

  }
}
