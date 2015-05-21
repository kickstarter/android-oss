package com.kickstarter.ui.view_holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ProjectStateChangedViewHolder extends ActivityListViewHolder {
  @InjectView(R.id.title) TextView title;
  @InjectView(R.id.project_photo) ImageView project_photo;

  public ProjectStateChangedViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    Picasso.with(view.getContext())
      .load(activity.project().photo().little())
      .into(project_photo);

    title.setText(titleText(activity));
  }

  public String titleText(final Activity activity) {
    switch (activity.category()) {
      case LAUNCH:
        // TODO: Switch to "You launched a project" if current user launched
        return view.getResources().getString(R.string.creator_launched_a_project, activity.user().name(), activity.project().name());
      case FAILURE:
        return view.getResources().getString(R.string.project_was_not_successfully_funded, activity.project().name());
      case CANCELLATION:
        return view.getResources().getString(R.string.project_was_cancelled_by_its_creator, activity.project().name());
      case SUSPENSION:
        return view.getResources().getString(R.string.project_was_suspended, activity.project().name());
      case RESUME:
        return view.getResources().getString(R.string.project_resumed, activity.project().name());
      default:
        return "";
    }
  }
}
