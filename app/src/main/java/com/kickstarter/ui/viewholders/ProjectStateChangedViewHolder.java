package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class ProjectStateChangedViewHolder extends ActivityListViewHolder {
  @Bind(R.id.title) TextView titleTextView;
  @Bind(R.id.project_photo) ImageView projectPhotoImageView;

  private final Delegate delegate;

  public interface Delegate {
    void projectStateChangedClicked(ProjectStateChangedViewHolder viewHolder, Activity activity);
  }

  public ProjectStateChangedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);

    titleTextView.setText(titleText(context, activity));
  }

  public String titleText(@NonNull final Context context, @NonNull final Activity activity) {
    switch (activity.category()) {
      case Activity.CATEGORY_FAILURE:
        return context.getString(R.string.___project_was_not_successfully_funded, activity.project().name());
      case Activity.CATEGORY_CANCELLATION:
        return context.getString(R.string.___project_was_cancelled_by_its_creator, activity.project().name());
      case Activity.CATEGORY_SUSPENSION:
        return context.getString(R.string.___project_was_suspended, activity.project().name());
      case Activity.CATEGORY_RESUME:
        return context.getString(R.string.___project_resumed, activity.project().name());
      default:
        return "";
    }
  }

  @OnClick(R.id.card_view)
  public void stateChangeCardClick() {
    delegate.projectStateChangedClicked(this, activity);
  }
}
