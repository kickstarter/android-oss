package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSCurrency;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.I18nUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Location;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectSocialActivity;
import com.kickstarter.ui.views.IconButton;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.DateTimeUtils.mediumDate;
import static com.kickstarter.libs.utils.DateTimeUtils.mediumDateShortTime;
import static com.kickstarter.libs.utils.ObjectUtils.coalesce;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;
import static com.kickstarter.libs.utils.ViewUtils.getScreenDensity;
import static com.kickstarter.libs.utils.ViewUtils.getScreenHeightDp;
import static com.kickstarter.libs.utils.ViewUtils.getScreenWidthDp;

public final class ProjectViewHolder extends KSViewHolder {
  private Project project;
  private String configCountry;
  private Context context;
  private final Delegate delegate;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.avatar_name) TextView avatarNameTextView;
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backer_label) LinearLayout backerLabelLinearLayout;
  protected @Bind(R.id.back_project_button) @Nullable Button backProjectButton;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.deadline_countdown_text_view) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit_text_view) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.project_disclaimer_text_view) TextView projectDisclaimerTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.land_overlay_text) @Nullable ViewGroup landOverlayTextViewGroup;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.manage_pledge_button) @Nullable Button managePledgeButton;
  protected @Bind(R.id.name_creator_view) @Nullable ViewGroup nameCreatorViewGroup;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.project_photo) ImageView photoImageView;
  protected @Bind(R.id.play_button_overlay) IconButton playButton;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_social_image) ImageView projectSocialImageView;
  protected @Bind(R.id.project_social_text) TextView projectSocialTextView;
  protected @Bind(R.id.project_stats_view) ViewGroup projectStatsViewGroup;
  protected @Bind(R.id.project_social_view) ViewGroup projectSocialViewGroup;
  protected @Bind(R.id.project_state_header_text_view) TextView projectStateHeaderTextView;
  protected @Bind(R.id.project_state_subhead_text_view) TextView projectStateSubheadTextView;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.view_pledge_button) @Nullable Button viewPledgeButton;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;
  protected @Bind(R.id.usd_conversion_text_view) TextView usdConversionTextView;

  protected @BindColor(R.color.green_alpha_20) int greenAlpha50Color;
  protected @BindColor(R.color.medium_gray) int mediumGrayColor;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;
  protected @BindDimen(R.dimen.grid_2) int grid2Dimen;
  protected @BindDimen(R.dimen.grid_3) int grid3Dimen;
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;

  protected @BindDrawable(R.drawable.click_indicator_light_masked) Drawable clickIndicatorLightMaskedDrawable;

  protected @BindString(R.string.project_creator_by_creator_html) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_blurb_read_more) String blurbReadMoreString;
  protected @BindString(R.string.discovery_baseball_card_stats_convert_from_pledged_of_goal) String convertedFromString;
  protected @BindString(R.string.project_disclaimer_goal_not_reached) String projectDisclaimerGoalNotReachedString;
  protected @BindString(R.string.project_disclaimer_goal_reached) String projectDisclaimerGoalReachedString;
  protected @BindString(R.string.project_status_funding_canceled) String fundingCanceledString;
  protected @BindString(R.string.project_status_funding_project_canceled_by_creator) String fundingCanceledByCreatorString;
  protected @BindString(R.string.project_status_project_was_successfully_funded_on_deadline) String successfullyFundedOnDeadlineString;
  protected @BindString(R.string.project_status_funding_suspended) String fundingSuspendedString;
  protected @BindString(R.string.project_status_funding_project_suspended) String fundingProjectSuspendedString;
  protected @BindString(R.string.project_status_funding_unsuccessful) String fundingUnsuccessfulString;
  protected @BindString(R.string.project_status_project_funding_goal_not_reached) String fundingGoalNotReachedString;
  protected @BindString(R.string.project_status_funded) String fundedString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal_short) String ofGoalString;
  protected @BindString(R.string.discovery_baseball_card_stats_backers) String backersString;

  @Inject KSCurrency ksCurrency;
  @Inject KSString ksString;

  public interface Delegate {
    void projectViewHolderBackProjectClicked(ProjectViewHolder viewHolder);
    void projectViewHolderBlurbClicked(ProjectViewHolder viewHolder);
    void projectViewHolderCommentsClicked(ProjectViewHolder viewHolder);
    void projectViewHolderCreatorClicked(ProjectViewHolder viewHolder);
    void projectViewHolderManagePledgeClicked(ProjectViewHolder viewHolder);
    void projectViewHolderUpdatesClicked(ProjectViewHolder viewHolder);
    void projectViewHolderVideoStarted(ProjectViewHolder viewHolder);
    void projectViewHolderViewPledgeClicked(ProjectViewHolder viewHolder);
  }

  public ProjectViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, String> projectAndCountry = requireNonNull((Pair<Project, String>) data);
    project = requireNonNull(projectAndCountry.first, Project.class);
    configCountry = requireNonNull(projectAndCountry.second, String.class);
  }

  public void onBind() {
    final Photo photo = project.photo();
    if (photo != null) {

      final int targetImageWidth = (int) (getScreenWidthDp(context) * getScreenDensity(context));
      Picasso.with(context)
        .load(photo.full())
        .resize(targetImageWidth, 0)
        .into(photoImageView);
      photoImageView.setAdjustViewBounds(true);
    }

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
    final Category category = project.category();
    if (category != null) {
      categoryTextView.setText(category.name());
    }
    final Location location = project.location();
    if (location != null) {
      locationTextView.setText(location.displayableName());
    }
    percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(project.percentageFunded()));
    deadlineCountdownTextView.setText(NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)));
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, context, ksString));
    backersCountTextView.setText(NumberUtils.format(project.backersCount()));

     /* Creator */
    Picasso.with(context).load(project.creator().avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);
    avatarNameTextView.setText(project.creator().name());
    final Integer updatesCount = project.updatesCount();
    updatesCountTextView.setText(updatesCount != null ? NumberUtils.format(updatesCount) : null);
    final Integer commentsCount = project.commentsCount();
    commentsCountTextView.setText(commentsCount != null ? NumberUtils.format(commentsCount) : null);

    setConvertedUsdView();
    setLandscapeActionButton();
    setLandscapeOverlayText();
    setPledgedOfGoalView();
    setProjectDisclaimerView();
    setProjectSocialClick();
    setProjectStateView();
    setSocialView();
    setStatsContentDescription();
  }

  @Nullable @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    delegate.projectViewHolderBackProjectClicked(this);
  }

  @OnClick({R.id.blurb, R.id.campaign})
  public void blurbClick() {
    delegate.projectViewHolderBlurbClicked(this);
  }

  @OnClick(R.id.comments)
  public void commentsClick() {
    delegate.projectViewHolderCommentsClicked(this);
  }

  @OnClick({R.id.creator_name, R.id.creator_info})
  public void creatorNameClick() {
    delegate.projectViewHolderCreatorClicked(this);
  }

  @Nullable @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    delegate.projectViewHolderManagePledgeClicked(this);
  }

  @OnClick(R.id.play_button_overlay)
  public void playButtonClick() {
    delegate.projectViewHolderVideoStarted(this);
  }

  @Nullable @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    delegate.projectViewHolderViewPledgeClicked(this);
  }

  public void setConvertedUsdView() {
    if (I18nUtils.isCountryUS(configCountry) && !I18nUtils.isCountryUS(project.country())) {
      usdConversionTextView.setVisibility(View.VISIBLE);
      usdConversionTextView.setText(ksString.format(
        convertedFromString,
        "pledged",
        ksCurrency.format(project.pledged(), project),
        "goal",
        ksCurrency.format(project.goal(), project)
      ));
    } else {
      usdConversionTextView.setVisibility(View.GONE);
    }
  }

  /**
   * Set landscape project action buttons in the ViewHolder rather than Activity.
   */
  public void setLandscapeActionButton() {
    if (backProjectButton != null && managePledgeButton != null && viewPledgeButton != null) {
      ProjectUtils.setActionButton(project, backProjectButton, managePledgeButton, viewPledgeButton);
    }
  }

  /**
   * Set top margin of overlay text based on landscape screen height, scaled by screen density.
   */
  public void setLandscapeOverlayText() {
    if (landOverlayTextViewGroup != null && nameCreatorViewGroup != null) {
      final int screenHeight = getScreenHeightDp(context);
      final float densityOffset = context.getResources().getDisplayMetrics().density;
      final float topMargin = ((screenHeight / 3 * 2) * densityOffset) - grid4Dimen;  // offset for toolbar
      ViewUtils.setRelativeViewGroupMargins(landOverlayTextViewGroup, grid4Dimen, (int) topMargin, grid4Dimen, 0);

      if (!project.hasVideo()) {
        ViewUtils.setRelativeViewGroupMargins(nameCreatorViewGroup, 0, 0, 0, grid2Dimen);
      } else {
        ViewUtils.setRelativeViewGroupMargins(nameCreatorViewGroup, 0, 0, 0, grid1Dimen);
      }
    }
  }

  public void setPledgedOfGoalView() {
    pledgedTextView.setText(ksCurrency.format(project.pledged(), project, false, true));

    /* a11y */
    final String goalString = ksCurrency.format(project.goal(), project, false, true);
    final String goalText = ViewUtils.isFontScaleLarge(context) ?
      ksString.format(ofGoalString, "goal", goalString) : ksString.format(pledgedOfGoalString, "goal", goalString);
    goalTextView.setText(goalText);
  }

  public void setProjectDisclaimerView() {
    final DateTime deadline = project.deadline();

    if (deadline == null) {
      projectDisclaimerTextView.setVisibility(View.GONE);
    } else if (!project.isLive()) {
      projectDisclaimerTextView.setVisibility(View.GONE);
    } else if (project.isFunded()) {
      projectDisclaimerTextView.setVisibility(View.VISIBLE);
      projectDisclaimerTextView.setText(ksString.format(
        projectDisclaimerGoalReachedString,
        "deadline",
        mediumDateShortTime(deadline)
      ));
    } else {
      projectDisclaimerTextView.setVisibility(View.VISIBLE);
      projectDisclaimerTextView.setText(ksString.format(
        projectDisclaimerGoalNotReachedString,
        "goal_currency",
        ksCurrency.format(project.goal(), project, true),
        "deadline",
        mediumDateShortTime(deadline)
      ));
    }
  }

  public void setProjectSocialClick() {
    if (project.isFriendBacking()) {
      if (project.friends().size() > 2) {
        projectSocialViewGroup.setBackground(clickIndicatorLightMaskedDrawable);
        projectSocialViewGroup.setOnClickListener(view -> {
          final BaseActivity activity = (BaseActivity) context;
          final Intent intent = new Intent(activity, ProjectSocialActivity.class)
            .putExtra(IntentKey.PROJECT, project);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
        });
      }
    }
  }

  public void setProjectStateView() {
    final DateTime stateChangedAt = coalesce(project.stateChangedAt(), new DateTime());

    switch(project.state()) {
      case Project.STATE_SUCCESSFUL:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        projectStateViewGroup.setBackgroundColor(greenAlpha50Color);

        projectStateHeaderTextView.setText(fundedString);
        projectStateSubheadTextView.setText(ksString.format(successfullyFundedOnDeadlineString,
          "deadline", mediumDate(stateChangedAt)
        ));
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        projectStateViewGroup.setBackgroundColor(mediumGrayColor);

        projectStateHeaderTextView.setText(fundingCanceledString);
        projectStateSubheadTextView.setText(fundingCanceledByCreatorString);
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        projectStateViewGroup.setBackgroundColor(mediumGrayColor);

        projectStateHeaderTextView.setText(fundingUnsuccessfulString);
        projectStateSubheadTextView.setText(ksString.format(fundingGoalNotReachedString,
          "deadline", mediumDate(stateChangedAt)
        ));
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        projectStateViewGroup.setBackgroundColor(mediumGrayColor);

        projectStateHeaderTextView.setText(fundingSuspendedString);
        projectStateSubheadTextView.setText(fundingProjectSuspendedString);
        break;
      default:
        percentageFundedProgressBar.setVisibility(View.VISIBLE);
        projectStateViewGroup.setVisibility(View.GONE);
        break;
    }
  }

  public void setSocialView() {
    if (project.isFriendBacking()) {
      projectSocialViewGroup.setVisibility(View.VISIBLE);
      ViewUtils.setLinearViewGroupMargins(projectStatsViewGroup, 0, grid3Dimen, 0, grid2Dimen);

      projectSocialImageView.setVisibility(View.VISIBLE);
      Picasso.with(context).load(project.friends().get(0).avatar()
        .small())
        .transform(new CircleTransformation())
        .into(projectSocialImageView);

      projectSocialTextView.setText(SocialUtils.projectCardFriendNamepile(project.friends(), ksString));

    } else {
      projectSocialViewGroup.setVisibility(View.GONE);
      ViewUtils.setLinearViewGroupMargins(projectStatsViewGroup, 0, grid3Dimen, 0, grid4Dimen);
    }
  }

  public void setStatsContentDescription() {
    final String backersCountContentDescription = NumberUtils.format(project.backersCount()) + " " +  backersString;
    final String pledgedContentDescription = pledgedTextView.getText() + " " + goalTextView.getText();
    final String deadlineCountdownContentDescription = deadlineCountdownTextView.getText() + " " + deadlineCountdownUnitTextView.getText();

    backersCountTextView.setContentDescription(backersCountContentDescription);
    pledgedTextView.setContentDescription(pledgedContentDescription);
    deadlineCountdownTextView.setContentDescription(deadlineCountdownContentDescription);
  }

  @OnClick(R.id.updates)
  public void updatesClick() {
    delegate.projectViewHolderUpdatesClicked(this);
  }
}
