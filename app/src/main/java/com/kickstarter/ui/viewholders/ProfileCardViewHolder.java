package com.kickstarter.ui.viewholders;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.libs.utils.ProgressBarUtils;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.ButterKnife;

public class ProfileCardViewHolder extends KSViewHolder {
  private final Delegate delegate;
  protected Project project;

  protected @Bind(R.id.funding_unsuccessful_text_view) TextView fundingUnsuccessfulTextView;
  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.profile_card_image) ImageView profileCardImageView;
  protected @Bind(R.id.profile_card_name) TextView profileCardNameTextView;
  protected @Bind(R.id.project_state_view_group) ViewGroup projectStateViewGroup;
  protected @Bind(R.id.successfully_funded_text_view) TextView successfullyFundedTextView;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  protected @BindString(R.string.profile_projects_status_successful) String successfulString;
  protected @BindString(R.string.profile_projects_status_unsuccessful) String unsuccessfulString;
  protected @BindString(R.string.profile_projects_status_canceled) String cancelledString;
  protected @BindString(R.string.profile_projects_status_suspended) String suspendedString;

  public interface Delegate {
    void profileCardViewHolderClicked(ProfileCardViewHolder viewHolder, Project project);
  }

  public ProfileCardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    project = ObjectUtils.requireNonNull((Project) data, Project.class);
  }

  @Override
  public void onBind() {
    final Photo photo = project.photo();

    if (photo != null) {
      profileCardImageView.setVisibility(View.VISIBLE);
      Picasso.with(view.getContext()).load(photo.med())
        .placeholder(grayGradientDrawable)
        .into(profileCardImageView);
    } else {
      profileCardImageView.setVisibility(View.INVISIBLE);
    }

    profileCardNameTextView.setText(project.name());
    percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(project.percentageFunded()));

    setProjectStateView();
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.profileCardViewHolderClicked(this, project);
  }

  public void setProjectStateView() {
    switch(project.state()) {
      case Project.STATE_SUCCESSFUL:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setVisibility(View.GONE);
        successfullyFundedTextView.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setText(successfulString);
        break;
      case Project.STATE_CANCELED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(cancelledString);
        break;
      case Project.STATE_FAILED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(unsuccessfulString);
        break;
      case Project.STATE_SUSPENDED:
        percentageFundedProgressBar.setVisibility(View.GONE);
        projectStateViewGroup.setVisibility(View.VISIBLE);
        successfullyFundedTextView.setVisibility(View.GONE);
        fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        fundingUnsuccessfulTextView.setText(suspendedString);
        break;
      default:
        percentageFundedProgressBar.setVisibility(View.VISIBLE);
        projectStateViewGroup.setVisibility(View.GONE);
        break;
    }
  }
}
