package com.kickstarter.ui.viewholders;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Project;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindDrawable;
import butterknife.ButterKnife;

public class ProfileCardViewHolder extends KSViewHolder {
  private final Delegate delegate;
  protected Project project;

  protected @Bind(R.id.percentage_funded) ProgressBar percentageFundedProgressBar;
  protected @Bind(R.id.profile_card_image) ImageView profileCardImageView;
  protected @Bind(R.id.profile_card_name) TextView profileCardNameTextView;

  protected @BindDrawable(R.drawable.gray_gradient) Drawable grayGradientDrawable;

  public interface Delegate {
    void projectCardClick(ProfileCardViewHolder viewHolder, Project project);
  }

  public ProfileCardViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    this.project = (Project) datum;

    Picasso.with(view.getContext()).load(project.photo().med())
      .placeholder(grayGradientDrawable)
      .into(profileCardImageView);
    profileCardNameTextView.setText(project.name());
    percentageFundedProgressBar.setProgress(Math.round(Math.min(100.0f, project.percentageFunded())));
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.projectCardClick(this, project);
  }
}
