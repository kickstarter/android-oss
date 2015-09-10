package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.kickstarter.ui.viewholders.ActivityListViewHolder;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProjectStateChangedViewHolder extends ActivityListViewHolder {
  @Bind(R.id.title) TextView titleTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;

  public ProjectStateChangedViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);

    titleTextView.setText(titleText(context, activity));
  }

  public String titleText(final Context context, final Activity activity) {
    switch (activity.category()) {
      case FAILURE:
        return context.getString(R.string.project_was_not_successfully_funded, activity.project().name());
      case CANCELLATION:
        return context.getString(R.string.project_was_cancelled_by_its_creator, activity.project().name());
      case SUSPENSION:
        return context.getString(R.string.project_was_suspended, activity.project().name());
      case RESUME:
        return context.getString(R.string.project_resumed, activity.project().name());
      default:
        return "";
    }
  }
}
