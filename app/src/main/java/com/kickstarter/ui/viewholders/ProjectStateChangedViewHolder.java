package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectStateChangedViewHolder extends ActivityListViewHolder {
  protected @Bind(R.id.title) TextView titleTextView;
  protected @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  protected @BindString(R.string.activity_project_state_change_project_was_not_successfully_funded) String projectNotSuccessfullyFundedString;
  protected @BindString(R.string.activity_project_state_change_project_was_cancelled_by_creator) String projectCanceledByCreatorString;
  protected @BindString(R.string.activity_project_state_change_project_was_suspended) String projectSuspendedString;

  private final Delegate delegate;

  @Inject KSString ksString;

  public interface Delegate {
    void projectStateChangedClicked(ProjectStateChangedViewHolder viewHolder, Activity activity);
  }

  public ProjectStateChangedViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind() {
    final Project project = activity.project();
    if (project == null) {
      return;
    }
    final User user = activity.user();
    if (user == null) {
      return;
    }
    final Photo photo = project.photo();
    if (photo == null) {
      return;
    }

    Picasso.with(view.getContext())
      .load(photo.little())
      .into(projectPhotoImageView);

    switch (activity.category()) {
      case Activity.CATEGORY_FAILURE:
        titleTextView.setText(ksString.format(projectNotSuccessfullyFundedString, "project_name", project.name()));
        break;
      case Activity.CATEGORY_CANCELLATION:
        titleTextView.setText(ksString.format(projectCanceledByCreatorString, "project_name", project.name()));
        break;
      case Activity.CATEGORY_SUSPENSION:
        titleTextView.setText(ksString.format(projectSuspendedString, "project_name", project.name()));
        break;
      default:
        titleTextView.setText("");
    }
  }

  @OnClick(R.id.card_view)
  public void stateChangeCardClick() {
    delegate.projectStateChangedClicked(this, activity);
  }
}
