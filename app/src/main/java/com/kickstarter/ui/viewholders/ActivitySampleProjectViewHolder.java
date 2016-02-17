package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivitySampleProjectViewHolder extends KSViewHolder {
  @Inject KSString ksString;

  protected @Bind(R.id.activity_click_area) LinearLayout activityClickArea;
  protected @Bind(R.id.activity_image) ImageView activityImageView;
  protected @Bind(R.id.activity_title) TextView activityTitleTextView;
  protected @Bind(R.id.activity_subtitle) TextView activitySubtitleTextView;
  protected @Bind(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_project_was_not_successfully_funded) String categoryFailureString;
  protected @BindString(R.string.activity_user_name_launched_project) String categoryLaunchString;
  protected @BindString(R.string.activity_successfully_funded) String categorySuccessString;
  protected @BindString(R.string.activity_funding_canceled) String categoryCancellationString;
  protected @BindString(R.string.activity_posted_update_number_title) String categoryUpdateString;

  private Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void activitySampleProjectViewHolderSeeActivityClicked(ActivitySampleProjectViewHolder viewHolder);
    void activitySampleProjectViewHolderProjectClicked(ActivitySampleProjectViewHolder viewHolder, Project project);
    void activitySampleProjectViewHolderUpdateClicked(ActivitySampleProjectViewHolder viewHolder, Activity activity);
  }

  public ActivitySampleProjectViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    activity = ObjectUtils.requireNonNull((Activity) data, Activity.class);
  }

  public void onBind() {
    final Context context = context();

    final Project project = activity.project();
    if (project != null) {

      final Photo photo = project.photo();
      if (photo != null) {
        Picasso.with(context)
          .load(photo.little())
          .into(activityImageView);
      }

      activityTitleTextView.setText(project.name());

      switch(activity.category()) {
        case Activity.CATEGORY_FAILURE:
          activitySubtitleTextView.setText(categoryFailureString);
          break;
        case Activity.CATEGORY_CANCELLATION:
          activitySubtitleTextView.setText(categoryCancellationString);
          break;
        case Activity.CATEGORY_LAUNCH:
          final User user = activity.user();
          if (user != null) {
            activitySubtitleTextView.setText(ksString.format(categoryLaunchString, "user_name", user.name()));
          }
          break;
        case Activity.CATEGORY_SUCCESS:
          activitySubtitleTextView.setText(categorySuccessString);
          break;
        case Activity.CATEGORY_UPDATE:
          final Update update = activity.update();
          if (update != null) {
            activitySubtitleTextView.setText(ksString.format(categoryUpdateString,
              "update_number", String.valueOf(update.sequence()),
              "update_title", update.title()));
          }
          break;
        default:
          break;
      }
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    delegate.activitySampleProjectViewHolderSeeActivityClicked(this);
  }

  @OnClick(R.id.activity_click_area)
  protected void activityProjectOnClick() {
    if (activity.category().equals(Activity.CATEGORY_UPDATE)) {
      delegate.activitySampleProjectViewHolderUpdateClicked(this, activity);
    } else {
      delegate.activitySampleProjectViewHolderProjectClicked(this, activity.project());
    }
  }
}
