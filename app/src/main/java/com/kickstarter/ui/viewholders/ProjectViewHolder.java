package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CircleTransform;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.libs.Money;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProjectViewHolder extends KsrViewHolder {

//  protected @Nullable @Bind(R.id.play_button_overlay) IconTextView playButtonIconTextView;
//  protected @Nullable @Bind(R.id.project_detail_video) VideoView videoView;
  protected @Bind(R.id.project_detail_photo) ImageView photoImageView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
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
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.avatar_name) TextView avatarNameTextView;
  protected @Bind(R.id.fund_message) TextView fundMessageTextView;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;
  @Inject Money money;

  private Project project;
  private final Delegate delegate;

  public interface Delegate {
    void takeBlurbClick();
    void takeCommentsClick();
    void takeCreatorNameClick();
    void takeShareClick();
    void takeUpdatesClick();
  }

  public ProjectViewHolder(final View view, final ProjectViewHolder.Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final Object datum) {
    project = (Project) datum;
    final Context context = view.getContext();

    /* Video */
    Picasso.with(context).load(project.photo().full()).into(photoImageView);

    /* Project */
    blurbTextView.setText(Html.fromHtml(context.getString(R.string.Blurb_read_more, project.blurb())));
    creatorNameTextView.setText(Html.fromHtml(context.getString(R.string.by_creator, project.creator().name())));
    projectNameTextView.setText(project.name());
    categoryTextView.setText(project.category().name());
    locationTextView.setText(project.location().displayableName());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    deadlineCountdownTextView.setText(Integer.toString(project.deadlineCountdownValue()));
    deadlineCountdownUnitTextView.setText(project.deadlineCountdownUnit(context));
    goalTextView.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    backersCountTextView.setText(project.formattedBackersCount());

     /* Creator */
    Picasso.with(context).load(project.creator().avatar()
      .medium())
      .transform(new CircleTransform())
      .into(avatarImageView);
    avatarNameTextView.setText(project.creator().name());
    fundMessageTextView.setText(String.format(context.getString(R.string.This_project_will_only_be_funded_if),
      money.formattedCurrency(project.goal(), project.currencyOptions(), true),
      project.deadline().toString(DateTimeUtils.writtenDeadline())));
    updatesCountTextView.setText(project.formattedUpdatesCount());
    commentsCountTextView.setText(project.formattedCommentsCount());
  }

  @OnClick({R.id.blurb, R.id.campaign})
  public void blurbClick() {
    delegate.takeBlurbClick();
  }

  @OnClick(R.id.comments)
  public void commentsClick() {
    delegate.takeCommentsClick();
  }

  @OnClick({R.id.creator_name, R.id.creator_info})
  public void creatorNameClick() {
    delegate.takeCreatorNameClick();
  }

  @OnClick(R.id.updates)
  public void updatesClick() {
    delegate.takeUpdatesClick();
  }

  @OnClick(R.id.share_button)
  public void shareProjectClick() {
    delegate.takeShareClick();
  }
}
