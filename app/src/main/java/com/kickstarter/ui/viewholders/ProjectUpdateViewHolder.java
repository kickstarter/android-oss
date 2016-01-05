package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DateTimeUtils;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

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

  private final Delegate delegate;

  protected @Inject KSString ksString;

  public interface Delegate {
    void projectUpdateProjectClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
    void projectUpdateClicked(ProjectUpdateViewHolder viewHolder, Activity activity);
  }

  public ProjectUpdateViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    super.onBind(datum);
    final Context context = view.getContext();

    projectNameTextView.setText(activity.project().name());
    Picasso.with(context)
      .load(activity.project().photo().little())
      .into(projectPhotoImageView);
    timestampTextView.setText(DateTimeUtils.relative(context, ksString, activity.update().publishedAt()));
    updateBodyTextView.setText(activity.update().truncatedBody());
    updateSequenceTextView.setText(ksString.format(
      projectUpdateCountString,
      "update_count",
      String.valueOf(activity.update().sequence())
    ));

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
