package com.kickstarter.ui.viewholders;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public final class ProfileCardViewHolder extends KSViewHolder {
  private final Delegate delegate;
  private Project project;

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
    this.project = ObjectUtils.requireNonNull((Project) data, Project.class);
  }

  @Override
  public void onBind() {
    final Photo photo = this.project.photo();

    if (photo != null) {
      this.profileCardImageView.setVisibility(View.VISIBLE);
      Picasso.get().load(photo.med())
        .placeholder(this.grayGradientDrawable)
        .into(this.profileCardImageView);
    } else {
      this.profileCardImageView.setVisibility(View.INVISIBLE);
    }

    this.profileCardNameTextView.setText(this.project.name());
    this.percentageFundedProgressBar.setProgress(ProgressBarUtils.progress(this.project.percentageFunded()));

    setProjectStateView();
  }

  @Override
  public void onClick(final @NonNull View view) {
    this.delegate.profileCardViewHolderClicked(this, this.project);
  }

  public void setProjectStateView() {
    switch(this.project.state()) {
      case Project.STATE_SUCCESSFUL:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.fundingUnsuccessfulTextView.setVisibility(View.GONE);
        this.successfullyFundedTextView.setVisibility(View.VISIBLE);
        this.successfullyFundedTextView.setText(this.successfulString);
        break;
      case Project.STATE_CANCELED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.successfullyFundedTextView.setVisibility(View.GONE);
        this.fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        this.fundingUnsuccessfulTextView.setText(this.cancelledString);
        break;
      case Project.STATE_FAILED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.successfullyFundedTextView.setVisibility(View.GONE);
        this.fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        this.fundingUnsuccessfulTextView.setText(this.unsuccessfulString);
        break;
      case Project.STATE_SUSPENDED:
        this.percentageFundedProgressBar.setVisibility(View.GONE);
        this.projectStateViewGroup.setVisibility(View.VISIBLE);
        this.successfullyFundedTextView.setVisibility(View.GONE);
        this.fundingUnsuccessfulTextView.setVisibility(View.VISIBLE);
        this.fundingUnsuccessfulTextView.setText(this.suspendedString);
        break;
      default:
        this.percentageFundedProgressBar.setVisibility(View.VISIBLE);
        this.projectStateViewGroup.setVisibility(View.GONE);
        break;
    }
  }
}
