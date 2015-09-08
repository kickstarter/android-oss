package com.kickstarter.ui.view_holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectUpdateViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.project_name) TextView projectNameTextView;
  @InjectView(R.id.project_photo) ImageView projectPhotoImageView;
  @InjectView(R.id.timestamp) TextView timestampTextView;
  @InjectView(R.id.update_body) TextView updateBodyTextView;
  @InjectView(R.id.update_sequence) TextView updateSequenceTextView;
  @InjectView(R.id.update_title) TextView updateTitleTextView;

  public ProjectUpdateViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    projectNameTextView.setText(activity.project().name());
    Picasso.with(view.getContext())
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    timestampTextView.setText(DateTimeUtils.relativeDateInWords(activity.update().publishedAt(), false, true));
    updateBodyTextView.setText(activity.update().truncatedBody());
    updateSequenceTextView.setText(view.getResources().getString(R.string.Update_sequence, activity.update().sequence()));
    updateTitleTextView.setText(activity.update().title());
  }
}
