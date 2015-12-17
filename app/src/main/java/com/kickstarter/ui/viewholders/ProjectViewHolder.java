package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.ui.activities.ProjectSocialActivity;
import com.kickstarter.ui.views.IconButton;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectViewHolder extends KSViewHolder {
  private Project project;
  private final Delegate delegate;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.avatar_name) TextView avatarNameTextView;
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backer_label) LinearLayout backerLabelLinearLayout;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.deadline_countdown_text_view) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit_text_view) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.funding_unsuccessful_view) TextView fundingUnsuccessfulTextView;
  protected @Bind(R.id.fund_message) TextView fundMessageTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.project_photo) ImageView photoImageView;
  protected @Bind(R.id.play_button_overlay) IconButton playButton;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.project_social_image) ImageView projectSocialImageView;
  protected @Bind(R.id.project_stats_view) ViewGroup projectStatsViewGroup;
  protected @Bind(R.id.project_social_text) TextView projectSocialTextView;
  protected @Bind(R.id.project_social_view) ViewGroup projectSocialViewGoup;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.successfully_funded_view) TextView successfullyFundedTextView;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;

  protected @BindDimen(R.dimen.grid_3) int grid3Dimen;
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;

  protected @BindString(R.string.project_creator_by_creator_html) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_blurb_read_more) String blurbReadMoreString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled) String bannerCanceledString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended) String bannerSuspendedString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_funding_unsuccessful_date) String fundingUnsuccessfulString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful) String bannerSuccessfulString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;
  protected @BindString(R.string.discovery_baseball_card_stats_backers) String backersString;

  @Inject KSString ksString;
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
    blurbTextView.setText(Html.fromHtml(ksString.format(blurbReadMoreString,
      "blurb", TextUtils.htmlEncode(project.blurb()),
      "space", "\u00A0"
    )));
    creatorNameTextView.setText(Html.fromHtml(ksString.format(byCreatorString,
      "creator_name", TextUtils.htmlEncode(project.creator().name()))));
    if (project.isBacking()) {
      backerLabelLinearLayout.setVisibility(View.VISIBLE);
    } else {
      backerLabelLinearLayout.setVisibility(View.GONE);
    }
    projectNameTextView.setText(project.name());
    categoryTextView.setText(project.category().name());
    locationTextView.setText(project.location().displayableName());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    deadlineCountdownTextView.setText(Integer.toString(ProjectUtils.deadlineCountdownValue(project)));
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, view.getContext(), ksString));
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
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
    final String goalText = money.formattedCurrency(project.goal(), project.currencyOptions(), true);
    if (ViewUtils.isFontScaleLarge(view.getContext())) {
      goalTextView.setText(goalText);
    } else {
      goalTextView.setText(ksString.format(pledgedOfGoalString,
        "goal", goalText
      ));
    }

    setProjectStateView();
    setSocialView();
    setStatsContentDescription();
  }

  // adjust spacing between stats and divider when social is present
  public void adjustStatsViewBottomMargin(final int bottomMargin) {
    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
      projectStatsViewGroup.getLayoutParams()
    );

    layoutParams.setMargins(grid3Dimen, grid3Dimen, grid3Dimen, bottomMargin);
    projectStatsViewGroup.setLayoutParams(layoutParams);
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

  @OnClick(R.id.project_social_view)
  public void projectSocialClick() {
    if (project.isFriendBacking()) {
      if (project.friends().size() > 2) {
        final BaseActivity activity = (BaseActivity) view.getContext();
        final Intent intent = new Intent(activity, ProjectSocialActivity.class)
          .putExtra(activity.getString(R.string.intent_project), project);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
      }
    }
  }

  public void setProjectStateView() {
    switch(project.state()) {
      case Project.STATE_SUCCESSFUL:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setText(bannerSuccessfulString);
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(bannerCanceledString);
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(fundingUnsuccessfulString,
          "date", project.formattedStateChangedAt()
        ));
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(bannerSuspendedString,
          "date", project.formattedStateChangedAt()
        ));
        break;
    }
  }

  public void setSocialView() {
    if (project.isFriendBacking()) {

      projectSocialViewGoup.setVisibility(View.VISIBLE);
      adjustStatsViewBottomMargin(grid3Dimen);

      projectSocialImageView.setVisibility(View.VISIBLE);
      Picasso.with(view.getContext()).load(project.friends().get(0).avatar()
        .small())
        .transform(new CircleTransformation())
        .into(projectSocialImageView);

      projectSocialTextView.setText(SocialUtils.projectCardFriendNamepile(project.friends(), ksString));

    } else {
      projectSocialViewGoup.setVisibility(View.GONE);
      adjustStatsViewBottomMargin(grid4Dimen);
    }
  }

  public void setStatsContentDescription() {
    final String backersCountContentDescription = project.formattedBackersCount() + " " +  backersString;
    final String pledgedContentDescription = pledgedTextView.getText() + " " + goalTextView.getText();
    final String deadlineCountdownContentDescription = deadlineCountdownTextView.getText() + " " + deadlineCountdownUnitTextView.getText();

    backersCountTextView.setContentDescription(backersCountContentDescription);
    pledgedTextView.setContentDescription(pledgedContentDescription);
    deadlineCountdownTextView.setContentDescription(deadlineCountdownContentDescription);
  }

  @OnClick(R.id.updates)
  public void updatesClick() {
    delegate.projectUpdatesClicked(this);
  }
}
