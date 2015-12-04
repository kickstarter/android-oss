package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.views.IconButton;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectViewHolder extends KSViewHolder {
  private Project project;
  private final Delegate delegate;

  protected @Bind(R.id.play_button_overlay) IconButton playButton;
  protected @Bind(R.id.project_photo) ImageView photoImageView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.backer_label) LinearLayout backerLabelLinearLayout;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.pledged_of_) TextView pledgedOfTextView;
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.avatar_name) TextView avatarNameTextView;
  protected @Bind(R.id.fund_message) TextView fundMessageTextView;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;

  protected @BindString(R.string.___backers) String backersString;
  protected @BindString(R.string.___of_) String ofString;
  protected @BindString(R.string.___pledged_of_) String pledgedOfString;
  protected @BindString(R.string.____to_go) String toGoString;

  @Inject Money money;

  public interface Delegate {
    void projectBlurbClicked(ProjectViewHolder viewHolder);
    void projectCommentsClicked(ProjectViewHolder viewHolder);
    void projectCreatorNameClicked(ProjectViewHolder viewHolder);
    void projectUpdatesClicked(ProjectViewHolder viewHolder);
    void projectVideoStarted(ProjectViewHolder viewHolder);
  }

  public ProjectViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    this.project = (Project) datum;
    final Context context = view.getContext();

    /* Video */
    Picasso.with(context).load(project.photo().full()).into(photoImageView);
    if (project.hasVideo()) {
      playButton.setVisibility(View.VISIBLE);
    } else {
      playButton.setVisibility(View.GONE);
    }

    /* Project */
    blurbTextView.setText(Html.fromHtml(context.getString(R.string.___Blurb_read_more, project.blurb())));
    creatorNameTextView.setText(Html.fromHtml(context.getString(R.string.___by_creator, project.creator().name())));
    if (project.isBacking()) {
      backerLabelLinearLayout.setVisibility(View.VISIBLE);
    } else {
      backerLabelLinearLayout.setVisibility(View.GONE);
    }
    projectNameTextView.setText(project.name());
    categoryTextView.setText(project.category().name());
    locationTextView.setText(project.location().displayableName());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    deadlineCountdownTextView.setText(Integer.toString(project.deadlineCountdownValue()));
    deadlineCountdownUnitTextView.setText(project.deadlineCountdownUnit(context));
    goalTextView.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    if (ViewUtils.isFontScaleLarge(view.getContext())) {
      pledgedOfTextView.setText(ofString);
    } else {
      pledgedOfTextView.setText(pledgedOfString);
    }
    backersCountTextView.setText(project.formattedBackersCount());

     /* Creator */
    Picasso.with(context).load(project.creator().avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);
    avatarNameTextView.setText(project.creator().name());
    fundMessageTextView.setText(String.format(context.getString(R.string.___This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
    updatesCountTextView.setText(project.formattedUpdatesCount());
    commentsCountTextView.setText(project.formattedCommentsCount());

    /* a11y */
    setStatsContentDescription();
  }

  @OnClick({R.id.blurb, R.id.campaign})
  public void blurbClick() {
    delegate.projectBlurbClicked(this);
  }

  @OnClick(R.id.comments)
  public void commentsClick() {
    delegate.projectCommentsClicked(this);
  }

  @OnClick({R.id.creator_name, R.id.creator_info})
  public void creatorNameClick() {
    delegate.projectCreatorNameClicked(this);
  }

  @OnClick(R.id.play_button_overlay)
  public void playButtonClick() {
    delegate.projectVideoStarted(this);
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

  @OnClick(R.id.updates)
  public void updatesClick() {
    delegate.projectUpdatesClicked(this);
  }
}
