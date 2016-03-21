package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.NumberUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.libs.utils.ProjectUtils;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.viewmodels.DiscoveryViewModel;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;
import static com.kickstarter.libs.utils.ViewUtils.getScreenDensity;
import static com.kickstarter.libs.utils.ViewUtils.getScreenWidthDp;

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
  protected @BindDimen(R.dimen.grid_4) int grid4Dimen;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.project_creator_by_creator) String byCreatorString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_canceled_date) String bannerCanceledDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_suspended_date) String bannerSuspendedDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_funding_unsuccessful_date) String fundingUnsuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_status_banner_successful_date) String bannerSuccessfulDateString;
  protected @BindString(R.string.discovery_baseball_card_metadata_featured_project) String featuredInString;
  protected @BindString(R.string.discovery_baseball_card_stats_pledged_of_goal) String pledgedOfGoalString;

  private Project project;
  private Context context;
  private final Delegate delegate;
  private DiscoveryViewModel viewModel;

  protected @Inject KSString ksString;

  public interface Delegate {
    void projectCardViewHolderClick(ProjectCardViewHolder viewHolder, Project project);
  }

  public ProjectCardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ((KSApplication) context.getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    project = requireNonNull((Project) data, Project.class);
  }

  public void onBind() {
    backersCountTextView.setText(NumberUtils.format(project.backersCount()));
    blurbTextView.setText(project.blurb());

    final Category category = project.category();
    if (category != null) {
      categoryTextView.setText(category.name());
    } else {
      categoryTextView.setText("");
    }

    deadlineCountdownTextView.setText(NumberUtils.format(ProjectUtils.deadlineCountdownValue(project)));
    deadlineCountdownUnitTextView.setText(ProjectUtils.deadlineCountdownDetail(project, context, ksString));
    nameTextView.setText(project.name());
    percentTextView.setText(NumberUtils.flooredPercentage(project.percentageFunded()));
    percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(project.percentageFunded()));

    final Photo photo = project.photo();
    if (photo != null) {
      photoImageView.setVisibility(View.VISIBLE);

      final int targetImageWidth = (int) (getScreenWidthDp(context) * getScreenDensity(context) - grid4Dimen);
      final int targetImageHeight = ProjectUtils.photoHeightFromWidthRatio(targetImageWidth);
      photoImageView.setMaxHeight(targetImageHeight);

      Picasso.with(context)
        .load(photo.full())
        .resize(targetImageWidth, targetImageHeight)  // required to fit properly into apis < 18
        .centerCrop()
        .placeholder(grayGradientDrawable)
        .into(photoImageView);

    } else {
      photoImageView.setVisibility(View.INVISIBLE);
    }

    setProjectMetadataView();
    setProjectStateView(context);
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.projectCardViewHolderClick(this, project);
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
    final DateTime stateChangedAt = ObjectUtils.coalesce(project.stateChangedAt(), new DateTime());

    switch(project.state()) {
      case Project.STATE_SUCCESSFUL:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setText(ksString.format(bannerSuccessfulDateString,
          "date", DateTimeUtils.relative(context, ksString, stateChangedAt)
        ));
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(bannerCanceledDateString,
          "date", DateTimeUtils.relative(context, ksString, stateChangedAt)
        ));
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(fundingUnsuccessfulDateString,
          "date", DateTimeUtils.relative(context, ksString, stateChangedAt)
        ));
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(ksString.format(bannerSuspendedDateString,
          "date", DateTimeUtils.relative(context, ksString, stateChangedAt)
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

      Picasso.with(context).load(project.friends().get(0).avatar()
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
    } else if (project.isStarred()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      starredViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      potdViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    } else if (project.isPotdToday()) {
      projectMetadataViewGroup.setVisibility(View.VISIBLE);
      potdViewGroup.setVisibility(View.VISIBLE);
      adjustCardViewTopMargin(grid1Dimen);

      backingViewGroup.setVisibility(View.GONE);
      starredViewGroup.setVisibility(View.GONE);
      featuredViewGroup.setVisibility(View.GONE);
    } else if (project.isFeaturedToday()) {
      final Category category = project.category();
      if (category != null) {
        final Category rootCategory = category.root();
        if (rootCategory != null) {
          projectMetadataViewGroup.setVisibility(View.VISIBLE);
          featuredViewGroup.setVisibility(View.VISIBLE);
          featuredTextView.setText(ksString.format(featuredInString,
            "category_name", rootCategory.name()));
          adjustCardViewTopMargin(grid1Dimen);

          backingViewGroup.setVisibility(View.GONE);
          starredViewGroup.setVisibility(View.GONE);
          potdViewGroup.setVisibility(View.GONE);
        }
      }
    } else {
      projectMetadataViewGroup.setVisibility(View.GONE);
      adjustCardViewTopMargin(0);
    }
  }
}
