package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.Activity;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DiscoveryActivityViewHolder extends KSViewHolder {
  @Inject KSString ksString;

  protected @Bind(R.id.activityImage) ImageView activityImageView;
  protected @Bind(R.id.activity_title) TextView activityTitleTextView;
  protected @Bind(R.id.activity_subtitle) TextView activitysubTitleTextView;
  protected @Bind(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_friend_backed_project_name_by_creator_name) String categoryBackingString;
  protected @BindString(R.string.activity_follow_back) String cateogryFollowString;
  protected @BindString(R.string.activity_project_was_not_successfully_funded) String categoryFailureString;
  protected @BindString(R.string.activity_user_name_launched_project) String categoryLaunchString;
  protected @BindString(R.string.activity_successfully_funded) String categorySuccessString;
  protected @BindString(R.string.activity_funding_canceled) String categoryCancellationString;
  protected @BindString(R.string.activity_posted_update_number_title) String categoryUpdateString;

  protected Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void seeActivityClick(DiscoveryActivityViewHolder viewHolder);
  }

  public DiscoveryActivityViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  public void onBind(final @NonNull Object datum) {
    this.activity = (Activity) datum;

    final Context context = view.getContext();

    if (activity.category() == Activity.CATEGORY_BACKING) {
      Picasso.with(context).load(activity.user().avatar()
        .small())
        .transform(new CircleTransformation())
        .into(activityImageView);

      activityTitleTextView.setVisibility(View.GONE);
      activitysubTitleTextView.setText(ksString.format(categoryBackingString, "friend_name", activity.user().name(),
        "project_name", activity.project().name(), "creator_name", activity.project().creator().name()));
    } else if (activity.category() == Activity.CATEGORY_FOLLOW) {
      Picasso.with(context).load(activity.user().avatar()
        .small())
        .transform(new CircleTransformation())
        .into(activityImageView);
      activityTitleTextView.setText(activity.project().name());
      activitysubTitleTextView.setText(cateogryFollowString);
    } else {
      Picasso.with(context)
        .load(activity.project().photo().little())
        .into(activityImageView);

      activityTitleTextView.setText(activity.project().name());

      switch(activity.category()) {
        case Activity.CATEGORY_FAILURE:
          activitysubTitleTextView.setText(categoryFailureString);
        case Activity.CATEGORY_CANCELLATION:
          activitysubTitleTextView.setText(categoryCancellationString);
        case Activity.CATEGORY_LAUNCH:
          activitysubTitleTextView.setText(ksString.format(categoryLaunchString, "user_name", activity.user().name()));
        case Activity.CATEGORY_SUCCESS:
          activitysubTitleTextView.setText(categorySuccessString);
        case Activity.CATEGORY_UPDATE:
          activitysubTitleTextView.setText(ksString.format(categoryUpdateString,
            "update_number", String.valueOf(activity.update().sequence()),
            "update_title", activity.update().title()));
      }

      // TODO: update width/height for image
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    delegate.seeActivityClick(this);
  }
}
