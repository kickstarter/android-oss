package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

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

  private final @Nullable Delegate delegate;
  private final KSString ksString;

  public interface Delegate {
    void projectStateChangedClicked(ProjectStateChangedViewHolder viewHolder, Activity activity);
  }

  public ProjectStateChangedViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind() {
    final Project project = activity().project();
    if (project == null) {
      return;
    }
    final User user = activity().user();
    if (user == null) {
      return;
    }
    final Photo photo = project.photo();
    if (photo == null) {
      return;
    }

    Picasso.get()
      .load(photo.little())
      .into(this.projectPhotoImageView);

    switch (activity().category()) {
      case Activity.CATEGORY_FAILURE:
        this.titleTextView.setText(this.ksString.format(this.projectNotSuccessfullyFundedString, "project_name", project.name()));
        break;
      case Activity.CATEGORY_CANCELLATION:
        this.titleTextView.setText(this.ksString.format(this.projectCanceledByCreatorString, "project_name", project.name()));
        break;
      case Activity.CATEGORY_SUSPENSION:
        this.titleTextView.setText(this.ksString.format(this.projectSuspendedString, "project_name", project.name()));
        break;
      default:
        this.titleTextView.setText("");
    }
  }

  @OnClick(R.id.card_view)
  public void stateChangeCardClick() {
    if (this.delegate != null) {
      this.delegate.projectStateChangedClicked(this, activity());
    }
  }
}
