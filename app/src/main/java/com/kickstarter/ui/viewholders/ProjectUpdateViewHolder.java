package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectUpdateViewHolder extends ActivityListViewHolder {
  @Bind(R.id.project_name) TextView projectNameTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  @Bind(R.id.timestamp) TextView timestampTextView;
  @Bind(R.id.update_body) TextView updateBodyTextView;
  @Bind(R.id.update_sequence) TextView updateSequenceTextView;
  @Bind(R.id.update_title) TextView updateTitleTextView;

  private final Delegate delegate;

  public interface Delegate {
    void projectUpdateProjectClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
    void projectUpdateClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
  }

  public ProjectUpdateViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
    super.onBind(datum);
    final Context context = view.getContext();

    projectNameTextView.setText(activity.project().name());
    Picasso.with(view.getContext())
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    timestampTextView.setText(DateTimeUtils.relativeDateInWords(activity.update().publishedAt(), false, true));
    updateBodyTextView.setText(activity.update().truncatedBody());
    updateSequenceTextView.setText(context.getString(R.string.Update_sequence, activity.update().sequence()));
    updateTitleTextView.setText(activity.update().title());
  }

  @OnClick(R.id.project_info)
  public void projectOnClick() {
    delegate.projectUpdateProjectClicked(this, activity);
  }

  @OnClick(R.id.update_info)
  public void updateOnClick() {
    delegate.projectUpdateClicked(this, activity);
  }
}
