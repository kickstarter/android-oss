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
  @InjectView(R.id.project_name) TextView project_name;
  @InjectView(R.id.project_photo) ImageView project_photo;
  @InjectView(R.id.timestamp) TextView timestamp;
  @InjectView(R.id.update_body) TextView update_body;
  @InjectView(R.id.update_sequence) TextView update_sequence;
  @InjectView(R.id.update_title) TextView update_title;

  public ProjectUpdateViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    project_name.setText(activity.project().name());
    Picasso.with(view.getContext())
      .load(activity.project().photo().little())
      .into(project_photo);
    timestamp.setText(DateTimeUtils.relativeDateInWords(activity.update().publishedAt(), false, true));
    update_body.setText(activity.update().truncatedBody());
    update_sequence.setText(view.getResources().getString(R.string.Update_sequence, activity.update().sequence()));
    update_title.setText(activity.update().title());
  }
}
