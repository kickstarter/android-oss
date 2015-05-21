package com.kickstarter.ui.view_holders;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.kickstarter.presenters.ActivityFeedPresenter;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class ProjectStateChangedPositiveViewHolder extends ActivityListViewHolder {
  @Optional @InjectView(R.id.project_photo) ImageView project_photo;
  public ProjectStateChangedPositiveViewHolder(final View view, final ActivityFeedPresenter presenter) {
    super(view, presenter);
    ButterKnife.inject(this, view);
  }

  @Override
  public void onBind(final Activity activity) {
    super.onBind(activity);

    Picasso.with(view.getContext())
      .load(activity.project().photo().full())
      .into(project_photo);
  }
}
