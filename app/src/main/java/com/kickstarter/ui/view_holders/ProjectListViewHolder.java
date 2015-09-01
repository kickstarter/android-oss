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
  protected @InjectView(R.id.backers_count) TextView backers_count;
  protected @InjectView(R.id.category) TextView category;
  protected @InjectView(R.id.deadline_countdown) TextView deadline_countdown;
  protected @InjectView(R.id.deadline_countdown_unit) TextView deadline_countdown_unit;
  protected @InjectView(R.id.goal) TextView goal;
  protected @InjectView(R.id.location) TextView location;
  protected @InjectView(R.id.name) TextView name;
  protected @InjectView(R.id.pledged) TextView pledged;
  protected @InjectView(R.id.percentage_funded) ProgressBar percentage_funded;
  protected @InjectView(R.id.photo) ImageView photo;
  protected @InjectView(R.id.photo_gradient) ViewGroup photo_gradient;
  protected @InjectView(R.id.potd_group) ViewGroup potd_group;
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

    backers_count.setText(project.formattedBackersCount());
    category.setText(project.category().name());
    deadline_countdown.setText(Integer.toString(project.deadlineCountdownValue()));
    deadline_countdown_unit.setText(project.deadlineCountdownUnit(view.getContext()));
    goal.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    location.setText(project.location().displayableName());
    pledged.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    name.setText(project.name());
    percentage_funded.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(view.getContext()).
      load(project.photo().full()).
      into(photo);

    final int potd_visible = project.isPotdToday() ? View.VISIBLE : View.INVISIBLE;
    photo_gradient.setVisibility(potd_visible);
    potd_group.setVisibility(potd_visible);

  }
}
