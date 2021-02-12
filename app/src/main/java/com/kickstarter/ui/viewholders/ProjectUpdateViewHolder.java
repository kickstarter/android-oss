package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectUpdateViewHolder extends ActivityListViewHolder {
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  protected @Bind(R.id.timestamp) TextView timestampTextView;
  protected @Bind(R.id.update_body) TextView updateBodyTextView;
  protected @Bind(R.id.update_sequence) TextView updateSequenceTextView;
  protected @Bind(R.id.update_title) TextView updateTitleTextView;

  protected @BindString(R.string.activity_project_update_update_count) String projectUpdateCountString;

  private final @Nullable Delegate delegate;
  private final KSString ksString;

  public interface Delegate {
    void projectUpdateProjectClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
    void projectUpdateClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
  }

  public ProjectUpdateViewHolder(final @NonNull View view, final @Nullable Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind() {
    final Context context = context();

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
    final Update update = activity().update();
    if (update == null) {
      return;
    }
    final DateTime publishedAt = ObjectUtils.coalesce(update.publishedAt(), new DateTime());

    this.projectNameTextView.setText(project.name());

    Picasso.get()
      .load(photo.little())
      .into(this.projectPhotoImageView);

    this.timestampTextView.setText(DateTimeUtils.relative(context, this.ksString, publishedAt));

    this.updateBodyTextView.setText(update.truncatedBody());

    this.updateSequenceTextView.setText(this.ksString.format(
      this.projectUpdateCountString,
      "update_count",
      String.valueOf(update.sequence())
    ));

    this.updateTitleTextView.setText(update.title());
  }

  @OnClick(R.id.project_info)
  public void projectOnClick() {
    if (this.delegate != null) {
      this.delegate.projectUpdateProjectClicked(this, activity());
    }
  }

  @OnClick(R.id.update_info)
  public void updateOnClick() {
    if (this.delegate != null) {
      this.delegate.projectUpdateClicked(this, activity());
    }
  }
}
