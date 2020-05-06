package com.kickstarter.ui.viewholders;

import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.Project;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.viewmodels.ProjectCardHolderViewModel;
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

import static com.kickstarter.libs.rx.transformers.Transformers.observeForUI;
import static com.kickstarter.libs.utils.ViewUtils.getScreenDensity;
import static com.kickstarter.libs.utils.ViewUtils.getScreenWidthDp;

public final class ProjectCardViewHolder extends KSViewHolder {
  private final ProjectCardHolderViewModel.ViewModel viewModel;
  private final KSString ksString;

  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backing_group) ViewGroup backingViewGroup;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.featured) TextView featuredTextView;
  protected @Bind(R.id.featured_group) ViewGroup featuredViewGroup;
  protected @Bind(R.id.friend_backing_avatar1) ImageView friendBackingAvatarImageView1;
  protected @Bind(R.id.friend_backing_avatar2) ImageView friendBackingAvatarImageView2;
  protected @Bind(R.id.friend_backing_avatar3) ImageView friendBackingAvatarImageView3;
  protected @Bind(R.id.friend_backing_message) TextView friendBackingMessageTextView;
  protected @Bind(R.id.friend_backing_group) ViewGroup friendBackingViewGroup;
  protected @Bind(R.id.funding_successful_date_text_view) TextView fundingSuccessfulTextViewDate;
  protected @Bind(R.id.funding_unsuccessful_view_group) ViewGroup fundingUnsuccessfulViewGroup;
  protected @Bind(R.id.funding_successful_view_group) ViewGroup fundingSuccessfulViewGroup;
  protected @Bind(R.id.funding_unsuccessful_text_view) TextView fundingUnsuccessfulTextView;
  protected @Bind(R.id.funding_unsuccessful_date_text_view) TextView fundingUnsuccessfulTextViewDate;
  protected @Nullable @Bind(R.id.land_card_view_group) ViewGroup landCardViewGroup;
  protected @Bind(R.id.location_container) ViewGroup locationContainer;
  protected @Bind(R.id.location_text_view) TextView locationTextView;
  protected @Bind(R.id.name_and_blurb_text_view) TextView nameAndBlurbTextView;
  protected @Bind(R.id.percent) TextView percentTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.project_card_view_group) ViewGroup projectCardViewGroup;
  protected @Bind(R.id.project_card_stats_view_group) ViewGroup projectCardStatsViewGroup;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.project_card_tags) ViewGroup projectTagContainerIsGone;
  protected @Bind(R.id.project_we_love_container) ViewGroup projectWeLoveContainer;
  protected @Bind(R.id.subcategory_container) ViewGroup subcategoryContainer;
  protected @Bind(R.id.subcategory_text_view) TextView subcategoryTextView;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.saved_view_group) ViewGroup savedViewGroup;

  protected @BindColor(R.color.ksr_dark_grey_400) int ksrDarkGrey400;
  protected @BindColor(R.color.ksr_soft_black) int ksrSoftBlack;

  protected @BindDimen(R.dimen.grid_1) int gridNew1Dimen;
  protected @BindDimen(R.dimen.grid_2) int gridNew2Dimen;
  protected @BindDimen(R.dimen.grid_3) int gridNew3Dimen;
  protected @BindDimen(R.dimen.grid_4) int gridNew4Dimen;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled) String fundingCanceledString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended_date) String bannerSuspendedDateString;
  protected @BindString(R.string.dashboard_creator_project_funding_unsuccessful) String fundingUnsuccessfulString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful_date) String bannerSuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  public interface Delegate {
    void projectCardViewHolderClicked(Project project);
  }

  public ProjectCardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.ksString = environment().ksString();
    this.viewModel = new ProjectCardHolderViewModel.ViewModel(environment());

    ButterKnife.bind(this, view);

    this.viewModel.outputs.backersCountTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.backersCountTextView::setText);

    this.viewModel.outputs.backingViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.backingViewGroup));

    this.viewModel.outputs.deadlineCountdownText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.deadlineCountdownTextView::setText);

    this.viewModel.outputs.featuredViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.featuredViewGroup));

    this.viewModel.outputs.friendAvatar2IsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(hidden -> ViewUtils.setGone(this.friendBackingAvatarImageView2, hidden));

    this.viewModel.outputs.friendAvatar3IsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(hidden -> ViewUtils.setGone(this.friendBackingAvatarImageView3, hidden));

    this.viewModel.outputs.friendAvatarUrl1()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url -> setFriendAvatarUrl(url, this.friendBackingAvatarImageView1));

    this.viewModel.outputs.friendAvatarUrl2()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url -> setFriendAvatarUrl(url, this.friendBackingAvatarImageView2));

    this.viewModel.outputs.friendAvatarUrl3()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(url -> setFriendAvatarUrl(url, this.friendBackingAvatarImageView3));

    this.viewModel.outputs.friendBackingViewIsHidden()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.friendBackingViewGroup));

    this.viewModel.outputs.friendsForNamepile()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(friends ->
        this.friendBackingMessageTextView.setText(SocialUtils.projectCardFriendNamepile(context(), friends, this.ksString))
      );

    this.viewModel.outputs.fundingUnsuccessfulViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.fundingUnsuccessfulViewGroup));

    this.viewModel.outputs.imageIsInvisible()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setInvisible(this.photoImageView));

    this.viewModel.outputs.locationName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.locationTextView::setText);

    this.viewModel.outputs.locationContainerIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.locationContainer));

    this.viewModel.outputs.nameAndBlurbText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setStyledNameAndBlurb);

    this.viewModel.outputs.notifyDelegateOfProjectClick()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(delegate::projectCardViewHolderClicked);

    this.viewModel.outputs.percentageFundedTextViewText()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentTextView::setText);

    this.viewModel.outputs.percentageFundedForProgressBar()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this.percentageFundedProgressBar::setProgress);

    this.viewModel.outputs.photoUrl()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::resizeProjectImage);

    this.viewModel.outputs.projectCanceledAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setCanceledTextView);

    this.viewModel.outputs.projectCardStatsViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectCardStatsViewGroup));

    this.viewModel.outputs.projectFailedAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setFailedAtTextView);

    this.viewModel.outputs.projectForDeadlineCountdownDetail()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDeadlineCountdownText);

    this.viewModel.outputs.projectStateViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectStateViewGroup));

    this.viewModel.outputs.projectSubcategoryName()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSubcategoryTextView);

    this.viewModel.outputs.projectSubcategoryIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.subcategoryContainer));

    this.viewModel.outputs.projectSuccessfulAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSuccessfullyFundedDateTextView);

    this.viewModel.outputs.projectSuspendedAt()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setSuspendedAtTextView);

    this.viewModel.outputs.projectTagContainerIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectTagContainerIsGone));

    this.viewModel.outputs.projectWeLoveIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectWeLoveContainer));

    this.viewModel.outputs.rootCategoryNameForFeatured()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(rootCategory ->
        this.featuredTextView.setText(this.ksString.format(this.featuredInString, "category_name", rootCategory))
      );

    this.viewModel.outputs.metadataViewGroupBackgroundDrawable()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(drawableRes -> this.projectMetadataViewGroup.setBackground(ContextCompat.getDrawable(this.context(), drawableRes)));

    this.viewModel.outputs.metadataViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.projectMetadataViewGroup));

    this.viewModel.outputs.savedViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.savedViewGroup));

    this.viewModel.outputs.fundingSuccessfulViewGroupIsGone()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(ViewUtils.setGone(this.fundingSuccessfulViewGroup));

    this.viewModel.outputs.setDefaultTopPadding()
      .compose(bindToLifecycle())
      .compose(observeForUI())
      .subscribe(this::setDefaultTopPadding);
  }
  private void setSubcategoryTextView(final @NonNull String subcategory) {
    this.subcategoryTextView.setText(subcategory);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Pair<Project, DiscoveryParams> projectAndParams = ObjectUtils.requireNonNull((Pair<Project, DiscoveryParams>) data);
    this.viewModel.inputs.configureWith(projectAndParams);
  }

  private void setStyledNameAndBlurb(final @NonNull Pair<String, String> nameAndBlurb) {
    final String nameString = ProjectUtils.isProjectNamePunctuated(nameAndBlurb.first)
      ? nameAndBlurb.first + " "
      : nameAndBlurb.first + ": ";

    final String blurbString = nameAndBlurb.second;

    final SpannableString styledString = new SpannableString(nameString + blurbString);

    styledString.setSpan(new ForegroundColorSpan(this.ksrSoftBlack), 0, nameString.length(), 0);

    styledString.setSpan(
      new ForegroundColorSpan(this.ksrDarkGrey400),
      nameString.length(),
      nameString.length() + blurbString.length(),
      0
    );

    this.nameAndBlurbTextView.setText(styledString);
  }

  private void resizeProjectImage(final @Nullable String avatarUrl) {
    final int targetImageWidth = (int) (getScreenWidthDp(context()) * getScreenDensity(context()) - this.gridNew4Dimen);
    final int targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth);
    this.photoImageView.setMaxHeight(targetImageHeight);

    Picasso.with(this.context())
      .load(avatarUrl)
      .resize(targetImageWidth, targetImageHeight)  // required to fit properly into apis < 18
      .centerCrop()
      .placeholder(this.grayGradientDrawable)
      .into(this.photoImageView);
  }

  private void setDeadlineCountdownText(final @NonNull Project project) {
    this.deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, context(), this.ksString));
  }

  private void setFriendAvatarUrl(final @NonNull String avatarUrl, final @NonNull ImageView imageView) {
    Picasso.with(context()).load(avatarUrl)
      .transform(new CircleTransformation())
      .into(imageView);
  }

  private void setDefaultTopPadding(final boolean setDefaultPadding) {
    if (setDefaultPadding) {
      adjustLandscapeTopPadding(this.landCardViewGroup, this.gridNew2Dimen, this.gridNew2Dimen, this.gridNew2Dimen, this.gridNew2Dimen);
      adjustViewGroupTopMargin(this.projectCardViewGroup, 0);
    } else {
      adjustLandscapeTopPadding(this.landCardViewGroup, this.gridNew2Dimen, this.gridNew3Dimen, this.gridNew2Dimen, this.gridNew2Dimen);
      adjustViewGroupTopMargin(this.projectCardViewGroup, this.gridNew1Dimen);
    }
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.viewModel.inputs.projectCardClicked();
  }

  /**
   *  Adjust spacing between cards when metadata label is present.
   */
  private void adjustViewGroupTopMargin(final @NonNull ViewGroup viewGroup, final int topMargin) {
    final RelativeLayout.MarginLayoutParams marginParams = new RelativeLayout.MarginLayoutParams(
      viewGroup.getLayoutParams()
    );

    marginParams.setMargins(0, topMargin, 0, 0);
    viewGroup.setLayoutParams(marginParams);
  }

  /**
   * Adjust card content spacing when metadata label is present.
   */
  private void adjustLandscapeTopPadding(final @Nullable ViewGroup landscapeViewGroup, final int left, final int top,
    final int right, final int bottom) {
    if (landscapeViewGroup != null) {
      landscapeViewGroup.setPadding(left, top, right, bottom);
    }
  }

  private void setCanceledTextView(final @NonNull DateTime projectCanceledAt) {
    this.fundingUnsuccessfulTextViewDate.setText(DateTimeUtils.relative(context(), this.ksString, projectCanceledAt));
    this.fundingUnsuccessfulTextView.setText(this.fundingCanceledString);
  }

  private void setSuccessfullyFundedDateTextView(final @NonNull DateTime projectSuccessfulAt) {
    this.fundingSuccessfulTextViewDate.setText(DateTimeUtils.relative(context(), this.ksString, projectSuccessfulAt));
  }

  private void setFailedAtTextView(final @NonNull DateTime projectFailedAt) {
    this.fundingUnsuccessfulTextViewDate.setText(DateTimeUtils.relative(context(), this.ksString, projectFailedAt));
    this.fundingUnsuccessfulTextView.setText(this.fundingUnsuccessfulString);

  }

  private void setSuspendedAtTextView(final @NonNull DateTime projectSuspendedAt) {
    this.fundingUnsuccessfulTextViewDate.setText(DateTimeUtils.relative(context(), this.ksString, projectSuspendedAt));
  }
}
