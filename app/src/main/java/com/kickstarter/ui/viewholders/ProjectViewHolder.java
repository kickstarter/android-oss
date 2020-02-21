package com.kickstarter.ui.viewholders;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.ProjectViewUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.ui.IntentKey;
import com.kickstarter.ui.activities.ProjectSocialActivity;
import com.kickstarter.ui.data.ProjectData;
import com.kickstarter.viewmodels.ProjectHolderViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.DateTimeUtils.mediumDate;
import static com.kickstarter.libs.utils.DateTimeUtils.mediumDateShortTime;
import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;
import static com.kickstarter.libs.utils.ViewUtils.getScreenDensity;
import static com.kickstarter.libs.utils.ViewUtils.getScreenHeightDp;
import static com.kickstarter.libs.utils.ViewUtils.getScreenWidthDp;

public final class ProjectViewHolder extends KSViewHolder {
  private ProjectHolderViewModel.ViewModel viewModel;
  private final Delegate delegate;
  private final KSString ksString;

  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backing_group) ViewGroup backingViewGroup;
  protected @Bind(R.id.back_project_button) @Nullable MaterialButton backProjectButton;
  protected @Bind(R.id.blurb_view) ViewGroup blurbViewGroup;
  protected @Bind(R.id.blurb_view_variant) ViewGroup blurbVariantViewGroup;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.blurb_variant) TextView blurbVariantTextView;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.comments_count) TextView commentsCountTextView;
  protected @Bind(R.id.usd_conversion_text_view) TextView conversionTextView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.deadline_countdown_text_view) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit_text_view) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.featured) TextView featuredTextView;
  protected @Bind(R.id.featured_group) ViewGroup featuredViewGroup;
  protected @Bind(R.id.project_disclaimer_text_view) TextView projectDisclaimerTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.land_overlay_text) @Nullable ViewGroup landOverlayTextViewGroup;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.manage_pledge_button) @Nullable MaterialButton managePledgeButton;
  protected @Bind(R.id.name_creator_view) @Nullable ViewGroup nameCreatorViewGroup;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.project_photo) ImageView photoImageView;
  protected @Bind(R.id.play_button_overlay) ImageButton playButton;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.project_action_buttons) @Nullable ViewGroup projectActionButtonsContainer;
  protected @Bind(R.id.project_dashboard_button) Button projectDashboardButton;
  protected @Bind(R.id.project_dashboard_container) ViewGroup projectDashboardContainer;
  protected @Bind(R.id.project_launch_date) TextView projectLaunchDateTextView;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_social_image) ImageView projectSocialImageView;
  protected @Bind(R.id.project_social_text) TextView projectSocialTextView;
  protected @Bind(R.id.project_stats_view) ViewGroup projectStatsViewGroup;
  protected @Bind(R.id.project_social_view) ViewGroup projectSocialViewGroup;
  protected @Bind(R.id.project_state_header_text_view) TextView projectStateHeaderTextView;
  protected @Bind(R.id.project_state_subhead_text_view) TextView projectStateSubheadTextView;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.view_pledge_button) @Nullable MaterialButton viewPledgeButton;
  protected @Bind(R.id.updates_count) TextView updatesCountTextView;

  protected @BindColor(R.color.green_alpha_20) int greenAlpha50Color;
  protected @BindColor(R.color.ksr_grey_400) int ksrGrey400;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;
  protected @BindDimen(R.dimen.grid_2) int grid2Dimen;
  protected @BindDimen(R.dimen.grid_3) int grid3Dimen;
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;
  protected @BindDimen(R.dimen.grid_10) int grid10Dimen;

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
    void projectViewHolderBlurbVariantClicked(ProjectViewHolder viewHolder);
    void projectViewHolderCommentsClicked(ProjectViewHolder viewHolder);
    void projectViewHolderCreatorClicked(ProjectViewHolder viewHolder);
    void projectViewHolderDashboardClicked(ProjectViewHolder viewHolder);
    void projectViewHolderManagePledgeClicked(ProjectViewHolder viewHolder);
    void projectViewHolderUpdatesClicked(ProjectViewHolder viewHolder);
    void projectViewHolderVideoStarted(ProjectViewHolder viewHolder);
    void projectViewHolderViewPledgeClicked(ProjectViewHolder viewHolder);
  }

  public ProjectViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.viewModel = new ProjectHolderViewModel.ViewModel(environment());
    this.delegate = delegate;
    this.ksString = environment().ksString();

    ButterKnife.bind(this, view);

    this.viewModel.outputs.avatarPhotoUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url ->
        Picasso.with(context())
          .load(url)
          .transform(new CircleTransformation())
          .into(this.avatarImageView)
      );

    this.viewModel.outputs.backersCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backersCountTextView::setText);

    this.viewModel.outputs.backingViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backingViewGroup));

    this.viewModel.outputs.blurbTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBlurbTextViews);

    this.viewModel.outputs.blurbVariantIsVisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setBlurbVariantVisibility);

    this.viewModel.outputs.categoryTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.categoryTextView::setText);

    this.viewModel.outputs.commentsCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.commentsCountTextView::setText);

    this.viewModel.outputs.creatorNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.creatorNameTextView::setText);

    this.viewModel.outputs.deadlineCountdownTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.deadlineCountdownTextView::setText);

    this.viewModel.outputs.featuredTextViewRootCategory()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(c ->
        this.featuredTextView.setText(this.ksString.format(this.featuredInString, "category_name", c))
      );

    this.viewModel.outputs.featuredViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.featuredViewGroup));

    this.viewModel.outputs.goalStringForTextView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setGoalTextView);

    this.viewModel.outputs.locationTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.locationTextView::setText);

    this.viewModel.outputs.projectOutput()
      .subscribe(p -> {
        // todo: break down these helpers
        setLandscapeOverlayText(p);
        setLandscapeActionButton(p);
        this.deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(p, context(), this.ksString));
      });

    this.viewModel.outputs.percentageFundedProgress()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentageFundedProgressBar::setProgress);

    this.viewModel.outputs.percentageFundedProgressBarIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.percentageFundedProgressBar));

    this.viewModel.outputs.playButtonIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.playButton));

    this.viewModel.outputs.pledgedTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.pledgedTextView::setText);

    this.viewModel.outputs.projectActionButtonContainerIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectActionButtonsContainerVisibility);

    this.viewModel.outputs.projectDashboardButtonText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectDashboardButton::setText);

    this.viewModel.outputs.projectDashboardContainerIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectDashboardContainer));

    this.viewModel.outputs.projectDisclaimerGoalNotReachedString()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectDisclaimerGoalNotReachedString);

    this.viewModel.outputs.projectDisclaimerGoalReachedDateTime()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectDisclaimerGoalReachedString);

    this.viewModel.outputs.projectDisclaimerTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectDisclaimerTextView));

    this.viewModel.outputs.projectLaunchDate()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectLaunchDateString);

    this.viewModel.outputs.projectLaunchDateIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectLaunchDateTextView));

    this.viewModel.outputs.projectMetadataViewGroupBackgroundDrawableInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(d -> this.projectMetadataViewGroup.setBackground(ContextCompat.getDrawable(context(), d)));

    this.viewModel.outputs.projectMetadataViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectMetadataViewGroup));

    this.viewModel.outputs.projectNameTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.projectNameTextView::setText);

    this.viewModel.outputs.projectPhoto()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setProjectPhoto);

    this.viewModel.outputs.projectSocialTextViewFriends()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(friends ->
        this.projectSocialTextView.setText(SocialUtils.projectCardFriendNamepile(context(), friends, this.ksString))
      );

    this.viewModel.outputs.projectSocialImageViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectSocialImageView));

    this.viewModel.outputs.projectSocialImageViewUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url ->
        Picasso.with(context()).load(url)
          .transform(new CircleTransformation())
          .into(this.projectSocialImageView)
      );

    this.viewModel.outputs.projectSocialViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectSocialViewGroup));

    this.viewModel.outputs.projectStateViewGroupBackgroundColorInt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(c -> this.projectStateViewGroup.setBackgroundColor(ContextCompat.getColor(context(), c)));

    this.viewModel.outputs.projectStateViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectStateViewGroup));

    this.viewModel.outputs.setCanceledProjectStateView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.setCanceledProjectStateView());

    this.viewModel.outputs.setProjectSocialClickListener()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.setProjectSocialClickListener());

    this.viewModel.outputs.setSuccessfulProjectStateView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSuccessfulProjectStateView);

    this.viewModel.outputs.setSuspendedProjectStateView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(__ -> this.setSuspendedProjectStateView());

    this.viewModel.outputs.setUnsuccessfulProjectStateView()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setUnsuccessfulProjectStateView);

    this.viewModel.outputs.shouldSetDefaultStatsMargins()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setStatsMargins);

    this.viewModel.outputs.startProjectSocialActivity()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::startProjectSocialActivity);

    this.viewModel.outputs.updatesCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.updatesCountTextView::setText);

    this.viewModel.outputs.conversionPledgedAndGoalText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setConvertedCurrencyView);

    this.viewModel.outputs.conversionTextViewIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.conversionTextView));
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    @SuppressWarnings("unchecked")
    final ProjectData projectData = requireNonNull((ProjectData) data);
    this.viewModel.inputs.configureWith(projectData);
  }

  private void setConvertedCurrencyView(final @NonNull Pair<String, String> pledgedAndGoal) {
    this.conversionTextView.setText(
      this.ksString.format(
        this.convertedFromString, "pledged", pledgedAndGoal.first, "goal", pledgedAndGoal.second
      )
    );
  }

  private void setGoalTextView(final @NonNull String goalString) {
    final String goalText = ViewUtils.isFontScaleLarge(context())
      ? this.ksString.format(this.ofGoalString, "goal", goalString)
      : this.ksString.format(this.pledgedOfGoalString, "goal", goalString);
    this.goalTextView.setText(goalText);
  }

  private void setProjectPhoto(final @NonNull Photo photo) {
    // Account for the grid2 start and end margins.
    final int targetImageWidth = (int) (getScreenWidthDp(context()) * getScreenDensity(context())) - this.grid2Dimen * 2;
    final int targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth);
    this.photoImageView.setMaxHeight(targetImageHeight);

    Picasso.with(context())
      .load(photo.full())
      .resize(targetImageWidth, targetImageHeight)
      .centerCrop()
      .placeholder(this.grayGradientDrawable)
      .into(this.photoImageView);
  }

  private void setCanceledProjectStateView() {
    this.projectStateHeaderTextView.setText(this.fundingCanceledString);
    this.projectStateSubheadTextView.setText(this.fundingCanceledByCreatorString);
  }

  private void setProjectActionButtonsContainerVisibility(final boolean gone) {
    if (this.projectActionButtonsContainer != null) {
      ViewUtils.setGone(this.projectActionButtonsContainer, gone);
    }
  }

  private void setBlurbTextViews(final String blurb) {
    final Spanned blurbHtml = Html.fromHtml(TextUtils.htmlEncode(blurb));
    this.blurbTextView.setText(blurbHtml);
    this.blurbVariantTextView.setText(blurbHtml);
  }

  private void setBlurbVariantVisibility(final boolean blurbVariantVisible) {
    ViewUtils.setGone(this.blurbViewGroup, blurbVariantVisible);
    ViewUtils.setGone(this.blurbVariantViewGroup, !blurbVariantVisible);
  }

  private void setProjectDisclaimerGoalReachedString(final @NonNull DateTime deadline) {
    this.projectDisclaimerTextView.setText(this.ksString.format(
      this.projectDisclaimerGoalReachedString,
      "deadline",
      mediumDateShortTime(deadline)
    ));
  }

  private void setProjectDisclaimerGoalNotReachedString(final @NonNull Pair<String, DateTime> goalAndDeadline) {
    this.projectDisclaimerTextView.setText(this.ksString.format(
      this.projectDisclaimerGoalNotReachedString,
      "goal_currency",
      goalAndDeadline.first,
      "deadline",
      mediumDateShortTime(goalAndDeadline.second)
    ));
  }

  private void setProjectLaunchDateString(final @NonNull String launchDate) {
    final SpannableString launchedDateSpannableString = new SpannableString(this.ksString.format(
      context().getString(R.string.You_launched_this_project_on_launch_date),
      "launch_date",
      launchDate
    ));

    ViewUtils.addBoldSpan(launchedDateSpannableString, launchDate);
    this.projectLaunchDateTextView.setText(launchedDateSpannableString);
  }

  private void setProjectSocialClickListener() {
    this.projectSocialViewGroup.setBackground(this.clickIndicatorLightMaskedDrawable);
    this.projectSocialViewGroup.setOnClickListener(__ -> this.viewModel.inputs.projectSocialViewGroupClicked());
  }

  private void setSuccessfulProjectStateView(final @NonNull DateTime stateChangedAt) {
    this.projectStateHeaderTextView.setText(this.fundedString);
    this.projectStateSubheadTextView.setText(
      this.ksString.format(this.successfullyFundedOnDeadlineString, "deadline", mediumDate(stateChangedAt))
    );
  }

  private void setSuspendedProjectStateView() {
    this.projectStateHeaderTextView.setText(this.fundingSuspendedString);
    this.projectStateSubheadTextView.setText(this.fundingProjectSuspendedString);
  }

  private void setUnsuccessfulProjectStateView(final @NonNull DateTime stateChangedAt) {
    this.projectStateHeaderTextView.setText(this.fundingUnsuccessfulString);
    this.projectStateSubheadTextView.setText(
      this.ksString.format(this.fundingGoalNotReachedString, "deadline", mediumDate(stateChangedAt))
    );
  }

  private void setStatsMargins(final boolean shouldSetDefaultMargins) {
    if (shouldSetDefaultMargins) {
      ViewUtils.setLinearViewGroupMargins(this.projectStatsViewGroup, 0, this.grid3Dimen, 0, this.grid2Dimen);
    } else {
      ViewUtils.setLinearViewGroupMargins(this.projectStatsViewGroup, 0, this.grid3Dimen, 0, this.grid4Dimen);
    }
  }

  private void startProjectSocialActivity(final @NonNull Project project) {
    final BaseActivity activity = (BaseActivity) context();
    final Intent intent = new Intent(context(), ProjectSocialActivity.class)
      .putExtra(IntentKey.PROJECT, project);
    activity.startActivity(intent);
    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_slide_out_left);
  }

  @Nullable @OnClick(R.id.back_project_button)
  public void backProjectButtonOnClick() {
    this.delegate.projectViewHolderBackProjectClicked(this);
  }

  @OnClick({R.id.blurb_view, R.id.campaign, R.id.read_more})
  public void blurbOnClick() {
    this.delegate.projectViewHolderBlurbClicked(this);
  }

  @OnClick({R.id.read_more})
  public void blurbVariantOnClick() {
    this.delegate.projectViewHolderBlurbVariantClicked(this);
  }

  @OnClick(R.id.comments)
  public void commentsOnClick() {
    this.delegate.projectViewHolderCommentsClicked(this);
  }

  @OnClick(R.id.creator_info)
  public void creatorNameOnClick() {
    this.delegate.projectViewHolderCreatorClicked(this);
  }

  @OnClick(R.id.project_dashboard_button)
  public void creatorDashboardOnClick() {
    this.delegate.projectViewHolderDashboardClicked(this);
  }

  @Nullable @OnClick(R.id.manage_pledge_button)
  public void managePledgeOnClick() {
    this.delegate.projectViewHolderManagePledgeClicked(this);
  }

  @OnClick(R.id.play_button_overlay)
  public void playButtonOnClick() {
    this.delegate.projectViewHolderVideoStarted(this);
  }

  @Nullable @OnClick(R.id.view_pledge_button)
  public void viewPledgeOnClick() {
    this.delegate.projectViewHolderViewPledgeClicked(this);
  }

  @OnClick(R.id.updates)
  public void updatesOnClick() {
    this.delegate.projectViewHolderUpdatesClicked(this);
  }

  /**
   * Set landscape project action buttons in the ViewHolder rather than Activity.
   */
  private void setLandscapeActionButton(final @NonNull Project project) {
    if (this.backProjectButton != null && this.managePledgeButton != null && this.viewPledgeButton != null) {
      ProjectViewUtils.setActionButton(project, this.backProjectButton, this.managePledgeButton, this.viewPledgeButton);
    }
  }

  /**
   * Set top margin of overlay text based on landscape screen height, scaled by screen density.
   */
  private void setLandscapeOverlayText(final @NonNull Project project) {
    if (this.landOverlayTextViewGroup != null && this.nameCreatorViewGroup != null) {
      final int screenHeight = getScreenHeightDp(context());
      final float densityOffset = context().getResources().getDisplayMetrics().density;
      final float topMargin = ((screenHeight / 3f * 2) * densityOffset) - this.grid10Dimen;
      ViewUtils.setRelativeViewGroupMargins(this.landOverlayTextViewGroup, this.grid4Dimen, (int) topMargin, this.grid4Dimen, 0);

      if (!project.hasVideo()) {
        ViewUtils.setRelativeViewGroupMargins(this.nameCreatorViewGroup, 0, 0, 0, this.grid2Dimen);
      } else {
        ViewUtils.setRelativeViewGroupMargins(this.nameCreatorViewGroup, 0, 0, 0, this.grid1Dimen);
      }
    }
  }
}
