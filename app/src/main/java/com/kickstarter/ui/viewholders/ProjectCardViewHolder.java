package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class ProjectCardViewHolder extends KSViewHolder {
  protected @Bind(R.id.backers_count) TextView backersCountTextView;
  protected @Bind(R.id.backing_group) ViewGroup backingViewGroup;
  protected @Bind(R.id.category) TextView categoryTextView;
  protected @Bind(R.id.deadline_countdown) TextView deadlineCountdownTextView;
  protected @Bind(R.id.deadline_countdown_unit) TextView deadlineCountdownUnitTextView;
  protected @Bind(R.id.featured) TextView featuredTextView;
  protected @Bind(R.id.featured_group) ViewGroup featuredViewGroup;
  protected @Bind(R.id.friend_backing_avatar) ImageView friendBackingAvatarImageView;
  protected @Bind(R.id.friend_backing_message) TextView friendBackingMessageTextView;
  protected @Bind(R.id.friend_backing_group) ViewGroup friendBackingViewGroup;
  protected @Bind(R.id.funding_unsuccessful_text_view) TextView fundingUnsuccessfulTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.percent) TextView percentTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.potd_view_group) ViewGroup potdViewGroup;
  protected @Bind(R.id.project_card_view_group) ViewGroup projectCardViewGroup;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.starred_view_group) ViewGroup starredViewGroup;
  protected @Bind(R.id.successfully_funded_text_view) TextView successfullyFundedTextView;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.project_creator_by_creator) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled_date) String bannerCanceledDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended_date) String bannerSuspendedDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_funding_unsuccessful_date) String fundingUnsuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful_date) String bannerSuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  protected Project project;
  private final Delegate delegate;
  protected DiscoveryViewModel viewModel;

  @Inject KSString ksString;

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

    backersCountTextView.setText(NumberUtils.format(project.backersCount()));
    blurbTextView.setText(project.blurb());
    categoryTextView.setText(project.category().name());
    deadlineCountdownTextView.setText(NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)));
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, view.getContext(), ksString));
    nameTextView.setText(project.name());
    percentTextView.setText(NumberUtils.flooredPercentage(project.percentageFunded()));
    percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(project.percentageFunded()));
    Picasso.with(view.getContext())
      .load(project.photo().full())
      .placeholder(grayGradientDrawable)
      .into(photoImageView);

    setProjectMetadataView();
    setProjectStateView(view.getContext());
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectCardClick(this, project);
  }

  // adjust spacing between cards when metadata label is present
  public void adjustCardViewTopMargin(final int topMargin) {
    final RelativeLayout.MarginLayoutParams marginParams = new RelativeLayout.MarginLayoutParams(
      projectCardViewGroup.getLayoutParams()
    );

    marginParams.setMargins(0, topMargin, 0, 0);
    projectCardViewGroup.setLayoutParams(marginParams);
  }

  public void setProjectStateView(final @NonNull Context context) {
    switch(project.state()) {
      case Project.STATE_SUCCESSFUL:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setText(ksString.format(bannerSuccessfulDateString,
          "date", DateTimeUtils.relative(context, ksString, project.stateChangedAt())
        ));
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(bannerCanceledDateString,
          "date", DateTimeUtils.relative(context, ksString, project.stateChangedAt())
        ));
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(fundingUnsuccessfulDateString,
          "date", DateTimeUtils.relative(context, ksString, project.stateChangedAt())
        ));
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(bannerSuspendedDateString,
          "date", DateTimeUtils.relative(context, ksString, project.stateChangedAt())
        ));
        break;
      default:
        percentageFundedProgressBar.setVisibility(View.VISIBLE);
        projectStateViewGroup.setVisibility(View.GONE);
        break;
    }
  }

  public void setProjectMetadataView() {

    // always show social
    if (project.isFriendBacking()) {
      friendBackingViewGroup.setVisibility(View.VISIBLE);

      Picasso.with(view.getContext()).load(project.friends().get(0).avatar()
        .small())
        .transform(new CircleTransformation())
        .into(friendBackingAvatarImageView);

      friendBackingMessageTextView.setText(SocialUtils.projectCardFriendNamepile(project.friends(), ksString));
    } else {
      friendBackingViewGroup.setVisibility(View.GONE);
    }

    if (project.isBacking()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      backingViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      starredViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isStarred()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      starredViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isPotdToday()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      potdViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isFeaturedToday() && project.category() != null) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      featuredViewGroup.setVisibility(View.VISIBLE);
      // TODO: Mini serialized category does not have access to root category name. This is a bug right now,
      // it's using the subcategory instead of the root category.
      featuredTextView.setText(ksString.format(featuredInString,
        "category_name", project.category().name()));
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
    }

    else {
      projectMetadataViewGroup.setVisibility(View.GONE);
      adjustCardViewTopMargin(0);
    }
  }
}
