package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDimen;
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
  protected @Bind(R.id.funding_unsuccessful_view) TextView fundingUnsuccessfulTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Nullable @Bind(R.id.created_by) TextView createdByTextView;
  protected @Nullable @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.percent) TextView percentTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.potd_view_group) ViewGroup potdViewGroup;
  protected @Bind(R.id.project_card_view_group) ViewGroup projectCardViewGroup;
  protected @Bind(R.id.project_metadata_view_group) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.starred_view_group) ViewGroup starredViewGroup;
  protected @Bind(R.id.successfully_funded_view) TextView successfullyFundedTextView;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;

  protected @BindString(R.string.project_creator_by_creator) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled) String bannerCanceledString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended) String bannerSuspendedString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_funding_unsuccessful_date) String fundingUnsuccessfulString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful) String bannerSuccessfulString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  protected Project project;
  private final Delegate delegate;
  protected DiscoveryViewModel viewModel;

  @Inject KSString ksString;
  @Inject Money money;

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

    backersCountTextView.setText(project.formattedBackersCount());
    blurbTextView.setText(project.blurb());
    categoryTextView.setText(project.category().name());
    deadlineCountdownTextView.setText(Integer.toString(ProjectUtils.deadlineCountdownValue(project)));
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, view.getContext(), ksString));
    nameTextView.setText(project.name());
    percentTextView.setText(StringUtils.displayFlooredPercentage(project.percentageFunded()));
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(view.getContext()).
      load(project.photo().full()).
      into(photoImageView);

    setProjectMetadataView();
    setProjectStateView();

    /* landscape-specific */
    if (createdByTextView != null) {
      createdByTextView.setText(Html.fromHtml(ksString.format(byCreatorString,
        "creator_name", TextUtils.htmlEncode(project.creator().name()))));
    }
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
