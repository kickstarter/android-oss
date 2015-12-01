package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Money;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.StringUtils;
import com.kickstarter.libs.utils.ViewUtils;
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
  protected @Bind(R.id.pledged_of_) TextView pledgedOfTextView;
  protected @Bind(R.id.goal) TextView goalTextView;
  protected @Bind(R.id.location) TextView locationTextView;
  protected @Bind(R.id.name) TextView nameTextView;
  protected @Nullable @Bind(R.id.created_by) TextView createdByTextView;
  protected @Nullable @Bind(R.id.blurb) TextView blurbTextView;
  protected @Bind(R.id.pledged) TextView pledgedTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.photo) ImageView photoImageView;
  protected @Bind(R.id.potd_group) ViewGroup potdViewGroup;
  protected @Bind(R.id.project_card_view_group) ViewGroup projectCardViewGroup;
  protected @Bind(R.id.project_metadata_view) ViewGroup projectMetadataViewGroup;
  protected @Bind(R.id.starred_group) ViewGroup starredViewGroup;
  protected @Bind(R.id.successfully_funded_view) TextView successfullyFundedTextView;

  protected @BindDimen(R.dimen.grid_1) int grid1Dimen;

  protected @BindString(R.string._is_a_backer) String oneFriendBackerString;
  protected @BindString(R.string._and_are_backers) String twoFriendBackersString;
  protected @BindString(R.string._and_more_are_backers) String manyFriendBackersString;
  protected @BindString(R.string.backers) String backersString;
  protected @BindString(R.string.Featured_in_) String featuredInString;
  protected @BindString(R.string.Funding_canceled) String fundingCanceledString;
  protected @BindString(R.string.Funding_suspended_) String fundingSuspendedString;
  protected @BindString(R.string.Funding_unsuccessful_) String fundingUnsuccessfulString;
  protected @BindString(R.string.of_) String ofString;
  protected @BindString(R.string.pledged_of_) String pledgedOfString;
  protected @BindString(R.string._to_go) String toGoString;

  protected Project project;
  private final Delegate delegate;
  protected DiscoveryViewModel viewModel;

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
    categoryTextView.setText(project.category().name());
    deadlineCountdownTextView.setText(Integer.toString(project.deadlineCountdownValue()));
    deadlineCountdownUnitTextView.setText(project.deadlineCountdownUnit(view.getContext()));
    goalTextView.setText(money.formattedCurrency(project.goal(), project.currencyOptions(), true));
    locationTextView.setText(project.location().displayableName());
    pledgedTextView.setText(money.formattedCurrency(project.pledged(), project.currencyOptions()));
    nameTextView.setText(project.name());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
    Picasso.with(view.getContext()).
      load(project.photo().full()).
      into(photoImageView);

    setProjectMetadataView();
    setProjectStateView();

    /* a11y */
    if (ViewUtils.isFontScaleLarge(view.getContext())) {
      pledgedOfTextView.setText(ofString);
    } else {
      pledgedOfTextView.setText(pledgedOfString);
    }

    setStatsContentDescription();

    /* landscape-specific */
    if (createdByTextView != null) {
      createdByTextView.setText(String.format(view.getContext().getString(R.string.by_), project.creator().name()));
    }

    if (blurbTextView != null) {
      blurbTextView.setText(project.blurb());
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
        fundingUnsuccessfulTextView.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.VISIBLE);
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(fundingCanceledString);
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(String.format(fundingUnsuccessfulString, project.formattedStateChangedAt()));
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(String.format(fundingSuspendedString, project.formattedStateChangedAt()));
        break;
    }
  }

  // only show one of either backer, social, starred, potd, or featured
  public void setProjectMetadataView() {

    if (project.isBacking()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      backingViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      friendBackingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isFriendBacking()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      friendBackingViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);

      Picasso.with(view.getContext()).load(project.friends().get(0).avatar()
        .small())
        .transform(new CircleTransformation())
        .into(friendBackingAvatarImageView);

      friendBackingMessageTextView.setText(StringUtils.friendBackingMetadataText(view.getContext(), project.friends()));
    }

    else if (project.isStarred()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      starredViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      friendBackingViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isPotdToday()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      potdViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      friendBackingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    }

    else if (project.isFeaturedToday()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      featuredViewGroup.setVisibility(View.VISIBLE);
      // TODO: mini serialized category does not have access to root category name
      featuredTextView.setText(String.format(featuredInString, project.category().baseImageName()));
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      friendBackingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
    }

    else {
      projectMetadataViewGroup.setVisibility(View.GONE);
      adjustCardViewTopMargin(0);
    }
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
}
