package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.math.RoundingMode;

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
  private String configCountry;
  private final Context context;
  private final Delegate delegate;
  private final KSCurrency ksCurrency;
  private final KSString ksString;
  private Project project;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backing_group) ViewGroup backingViewGroup;
  protected @Bind(R.id.back_project_button) @Nullable Button backProjectButton;
  protected @Bind(R.id.blurb_view) ViewGroup blurbViewGroup;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.deadline_countdown_text_view) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit_text_view) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.featured) TextView featuredTextView;
  protected @Bind(R.id.featured_group) ViewGroup featuredViewGroup;
  protected @Bind(R.id.project_disclaimer_text_view) TextView projectDisclaimerTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.land_overlay_text) @Nullable ViewGroup landOverlayTextViewGroup;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.manage_pledge_button) @Nullable Button managePledgeButton;
  protected @Bind(R.id.name_creator_view) @Nullable ViewGroup nameCreatorViewGroup;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.project_photo) ImageView photoImageView;
  protected @Bind(R.id.play_button_overlay) ImageButton playButton;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
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
  protected @BindColor(R.color.ksr_grey_400) int ksrGrey400;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;
  protected @BindDimen(R.dimen.grid_2) int grid2Dimen;
  protected @BindDimen(R.dimen.grid_3) int grid3Dimen;
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;

  protected @BindDrawable(R.drawable.click_indicator_light_masked) Drawable clickIndicatorLightMaskedDrawable;
  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.project_creator_by_creator_html) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_blurb_read_more) String blurbReadMoreString;
  protected @BindString(R.string.discovery_baseball_card_stats_convert_from_pledged_of_goal) String convertedFromString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
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

  public ProjectViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();
    this.ksCurrency = environment().ksCurrency();
    this.ksString = environment().ksString();

    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final Pair<Project, String> projectAndCountry = requireNonNull((Pair<Project, String>) data);
    this.project = requireNonNull(projectAndCountry.first, Project.class);
    this.configCountry = requireNonNull(projectAndCountry.second, String.class);
  }

  public void onBind() {
    final Photo photo = this.project.photo();
    if (photo != null) {
      // Account for the grid2 start and end margins.
      final int targetImageWidth = (int) (getScreenWidthDp(this.context) * getScreenDensity(this.context)) - this.grid2Dimen * 2;
      final int targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth);
      this.photoImageView.setMaxHeight(targetImageHeight);

      Picasso.with(this.context)
        .load(photo.full())
        .resize(targetImageWidth, targetImageHeight)
        .centerCrop()
        .placeholder(this.grayGradientDrawable)
        .into(this.photoImageView);
    }

    if (this.project.hasVideo()) {
      this.playButton.setVisibility(View.VISIBLE);
    } else {
      this.playButton.setVisibility(View.GONE);
    }

    /* Project */
    this.blurbTextView.setText(Html.fromHtml(TextUtils.htmlEncode(this.project.blurb())));
    this.creatorNameTextView.setText(Html.fromHtml(this.ksString.format(this.byCreatorString,
      "creator_name", TextUtils.htmlEncode(this.project.creator().name()))));
    this.projectNameTextView.setText(this.project.name());
    final Category category = this.project.category();
    if (category != null) {
      this.categoryTextView.setText(category.name());
    }
    final Location location = this.project.location();
    if (location != null) {
      this.locationTextView.setText(location.displayableName());
    }
    this.percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(this.project.percentageFunded()));
    this.deadlineCountdownTextView.setText(NumberUtils.format(ProjectUtils.deadlineCountdownValue(this.project)));
    this.deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(this.project, this.context, this.ksString));
    this.backersCountTextView.setText(NumberUtils.format(this.project.backersCount()));

    /* Creator */
    Picasso.with(this.context).load(this.project.creator().avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(this.avatarImageView);
    final Integer updatesCount = this.project.updatesCount();
    this.updatesCountTextView.setText(updatesCount != null ? NumberUtils.format(updatesCount) : null);
    final Integer commentsCount = this.project.commentsCount();
    this.commentsCountTextView.setText(commentsCount != null ? NumberUtils.format(commentsCount) : null);

    setConvertedUsdView();
    setLandscapeActionButton();
    setLandscapeOverlayText();
    setPledgedOfGoalView();
    setProjectDisclaimerView();
    setProjectMetadataLozenge();
    setProjectSocialClick();
    setProjectStateView();
    setSocialView();
    setStatsContentDescription();
  }

  @Nullable @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    this.delegate.projectViewHolderBackProjectClicked(this);
  }

  @OnClick({R.id.blurb_view, R.id.campaign})
  public void blurbClick() {
    this.delegate.projectViewHolderBlurbClicked(this);
  }

  @OnClick(R.id.comments)
  public void commentsClick() {
    this.delegate.projectViewHolderCommentsClicked(this);
  }

  @OnClick({R.id.creator_name, R.id.creator_info})
  public void creatorNameClick() {
    this.delegate.projectViewHolderCreatorClicked(this);
  }

  @Nullable @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    this.delegate.projectViewHolderManagePledgeClicked(this);
  }

  @OnClick(R.id.play_button_overlay)
  public void playButtonClick() {
    this.delegate.projectViewHolderVideoStarted(this);
  }

  @Nullable @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    this.delegate.projectViewHolderViewPledgeClicked(this);
  }

  public void setConvertedUsdView() {
    if (I18nUtils.isCountryUS(this.configCountry) && !I18nUtils.isCountryUS(this.project.country())) {
      this.usdConversionTextView.setVisibility(View.VISIBLE);
      this.usdConversionTextView.setText(this.ksString.format(
        this.convertedFromString,
        "pledged",
        this.ksCurrency.format(this.project.pledged(), this.project),
        "goal",
        this.ksCurrency.format(this.project.goal(), this.project)
      ));
    } else {
      this.usdConversionTextView.setVisibility(View.GONE);
    }
  }

  /**
   * Set landscape project action buttons in the ViewHolder rather than Activity.
   */
  public void setLandscapeActionButton() {
    if (this.backProjectButton != null && this.managePledgeButton != null && this.viewPledgeButton != null) {
      ProjectUtils.setActionButton(this.project, this.backProjectButton, this.managePledgeButton, this.viewPledgeButton);
    }
  }

  /**
   * Set top margin of overlay text based on landscape screen height, scaled by screen density.
   */
  public void setLandscapeOverlayText() {
    if (this.landOverlayTextViewGroup != null && this.nameCreatorViewGroup != null) {
      final int screenHeight = getScreenHeightDp(this.context);
      final float densityOffset = this.context.getResources().getDisplayMetrics().density;
      final float topMargin = ((screenHeight / 3 * 2) * densityOffset) - this.grid4Dimen;  // offset for toolbar
      ViewUtils.setRelativeViewGroupMargins(this.landOverlayTextViewGroup, this.grid4Dimen, (int) topMargin, this.grid4Dimen, 0);

      if (!this.project.hasVideo()) {
        ViewUtils.setRelativeViewGroupMargins(this.nameCreatorViewGroup, 0, 0, 0, this.grid2Dimen);
      } else {
        ViewUtils.setRelativeViewGroupMargins(this.nameCreatorViewGroup, 0, 0, 0, this.grid1Dimen);
      }
    }
  }

  public void setPledgedOfGoalView() {
    this.pledgedTextView.setText(this.ksCurrency.format(this.project.pledged(), this.project, false, true, RoundingMode.DOWN));

    /* a11y */
    final String goalString = this.ksCurrency.format(this.project.goal(), this.project, false, true, RoundingMode.DOWN);
    final String goalText = ViewUtils.isFontScaleLarge(this.context)
      ? this.ksString.format(this.ofGoalString, "goal", goalString)
      : this.ksString.format(this.pledgedOfGoalString, "goal", goalString);
    this.goalTextView.setText(goalText);
  }

  public void setProjectDisclaimerView() {
    final DateTime deadline = this.project.deadline();

    if (deadline == null) {
      this.projectDisclaimerTextView.setVisibility(View.GONE);
    } else if (!this.project.isLive()) {
      this.projectDisclaimerTextView.setVisibility(View.GONE);
    } else if (this.project.isFunded()) {
      this.projectDisclaimerTextView.setVisibility(View.VISIBLE);
      this.projectDisclaimerTextView.setText(this.ksString.format(
        this.projectDisclaimerGoalReachedString,
        "deadline",
        mediumDateShortTime(deadline)
      ));
    } else {
      this.projectDisclaimerTextView.setVisibility(View.VISIBLE);
      this.projectDisclaimerTextView.setText(this.ksString.format(
        this.projectDisclaimerGoalNotReachedString,
        "goal_currency",
        this.ksCurrency.format(this.project.goal(), this.project, true),
        "deadline",
        mediumDateShortTime(deadline)
      ));
    }
  }

  private void setProjectMetadataLozenge() {
    final ProjectUtils.Metadata metadata = ProjectUtils.metadataForProject(this.project);
    if (metadata == ProjectUtils.Metadata.BACKING) {
      this.projectMetadataViewGroup.setBackground(ContextCompat.getDrawable(this.context, R.drawable.rect_green_grey_stroke));
      this.backingViewGroup.setVisibility(View.VISIBLE);
    } else if (metadata == ProjectUtils.Metadata.CATEGORY_FEATURED) {
      this.featuredViewGroup.setVisibility(View.VISIBLE);
      final Category category = this.project.category();
      if (category != null && category.root() != null) {
        final String rootCategory = category.root().name();
        this.featuredTextView.setText(this.ksString.format(this.featuredInString, "category_name", rootCategory));
      }
    } else {
      this.projectMetadataViewGroup.setVisibility(View.GONE);
    }
  }

  public void setProjectSocialClick() {
    if (this.project.isFriendBacking()) {
      if (this.project.friends().size() > 2) {
        this.projectSocialViewGroup.setBackground(this.clickIndicatorLightMaskedDrawable);
        this.projectSocialViewGroup.setOnClickListener(view -> {
          final BaseActivity activity = (BaseActivity) this.context;
          final Intent intent = new Intent(activity, ProjectSocialActivity.class)
            .putExtra(IntentKey.PROJECT, this.project);
          activity.startActivity(intent);
          activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
        });
      }
    }
  }

  public void setProjectStateView() {
    final DateTime stateChangedAt = coalesce(this.project.stateChangedAt(), new DateTime());

    switch(this.project.state()) {
      case Project.STATE_SUCCESSFUL:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setBackgroundColor(this.greenAlpha50Color);

        this.projectStateHeaderTextView.setText(this.fundedString);
        this.projectStateSubheadTextView.setText(
          this.ksString.format(this.successfullyFundedOnDeadlineString, "deadline", mediumDate(stateChangedAt))
        );
        break;
      case Project.STATE_CANCELED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setBackgroundColor(this.ksrGrey400);

        this.projectStateHeaderTextView.setText(this.fundingCanceledString);
        this.projectStateSubheadTextView.setText(this.fundingCanceledByCreatorString);
        break;
      case Project.STATE_FAILED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setBackgroundColor(this.ksrGrey400);

        this.projectStateHeaderTextView.setText(this.fundingUnsuccessfulString);
        this.projectStateSubheadTextView.setText(
          this.ksString.format(this.fundingGoalNotReachedString, "deadline", mediumDate(stateChangedAt))
        );
        break;
      case Project.STATE_SUSPENDED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setBackgroundColor(this.ksrGrey400);

        this.projectStateHeaderTextView.setText(this.fundingSuspendedString);
        this.projectStateSubheadTextView.setText(this.fundingProjectSuspendedString);
        break;
      default:
        this.percentageFundedProgressBar.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setVisibility(View.GONE);
        break;
    }
  }

  public void setSocialView() {
    if (this.project.isFriendBacking()) {
      this.projectSocialViewGroup.setVisibility(View.VISIBLE);
      ViewUtils.setLinearViewGroupMargins(this.projectStatsViewGroup, 0, this.grid3Dimen, 0, this.grid2Dimen);

      this.projectSocialImageView.setVisibility(View.VISIBLE);
      Picasso.with(this.context).load(this.project.friends().get(0).avatar()
        .small())
        .transform(new CircleTransformation())
        .into(this.projectSocialImageView);

      this.projectSocialTextView.setText(SocialUtils.projectCardFriendNamepile(context(), this.project.friends(), this.ksString));

    } else {
      this.projectSocialViewGroup.setVisibility(View.GONE);
      ViewUtils.setLinearViewGroupMargins(this.projectStatsViewGroup, 0, this.grid3Dimen, 0, this.grid4Dimen);
    }
  }

  public void setStatsContentDescription() {
    final String backersCountContentDescription = NumberUtils.format(this.project.backersCount()) + " " +  this.backersString;
    final String pledgedContentDescription = this.pledgedTextView.getText() + " " + this.goalTextView.getText();
    final String deadlineCountdownContentDescription = this.deadlineCountdownTextView.getText() + " " + this.deadlineCountdownUnitTextView.getText();

    this.backersCountTextView.setContentDescription(backersCountContentDescription);
    this.pledgedTextView.setContentDescription(pledgedContentDescription);
    this.deadlineCountdownTextView.setContentDescription(deadlineCountdownContentDescription);
  }

  @OnClick(R.id.updates)
  public void updatesClick() {
    this.delegate.projectViewHolderUpdatesClicked(this);
  }
}
