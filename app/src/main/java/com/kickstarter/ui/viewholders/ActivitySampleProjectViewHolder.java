package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.Update;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivitySampleProjectViewHolder extends KSViewHolder {
  private Activity activity;
  private final KSString ksString;

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

  private final Delegate delegate;
  public interface Delegate {
    void activitySampleProjectViewHolderSeeActivityClicked(ActivitySampleProjectViewHolder viewHolder);
    void activitySampleProjectViewHolderProjectClicked(ActivitySampleProjectViewHolder viewHolder, Project project);
    void activitySampleProjectViewHolderUpdateClicked(ActivitySampleProjectViewHolder viewHolder, Activity activity);
  }

  public ActivitySampleProjectViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.ksString = environment().ksString();
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.activity = ObjectUtils.requireNonNull((Activity) data, Activity.class);
  }

  public void onBind() {
    final Context context = context();

    final Project project = this.activity.project();
    if (project != null) {

      final Photo photo = project.photo();
      if (photo != null) {
        Picasso.get()
          .load(photo.little())
          .into(this.activityImageView);
      }

      this.activityTitleTextView.setText(project.name());

      switch(this.activity.category()) {
        case Activity.CATEGORY_FAILURE:
          this.activitySubtitleTextView.setText(this.categoryFailureString);
          break;
        case Activity.CATEGORY_CANCELLATION:
          this.activitySubtitleTextView.setText(this.categoryCancellationString);
          break;
        case Activity.CATEGORY_LAUNCH:
          final User user = this.activity.user();
          if (user != null) {
            this.activitySubtitleTextView.setText(
              this.ksString.format(this.categoryLaunchString, "user_name", user.name())
            );
          }
          break;
        case Activity.CATEGORY_SUCCESS:
          this.activitySubtitleTextView.setText(this.categorySuccessString);
          break;
        case Activity.CATEGORY_UPDATE:
          final Update update = this.activity.update();
          if (update != null) {
            this.activitySubtitleTextView.setText(
              this.ksString.format(
                this.categoryUpdateString,
                "update_number", String.valueOf(update.sequence()),
                "update_title", update.title()
              )
            );
          }
          break;
        default:
          break;
      }
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    this.delegate.activitySampleProjectViewHolderSeeActivityClicked(this);
  }

  @OnClick(R.id.activity_click_area)
  protected void activityProjectOnClick() {
    if (this.activity.category().equals(Activity.CATEGORY_UPDATE)) {
      this.delegate.activitySampleProjectViewHolderUpdateClicked(this, this.activity);
    } else {
      this.delegate.activitySampleProjectViewHolderProjectClicked(this, this.activity.project());
    }
  }
}
