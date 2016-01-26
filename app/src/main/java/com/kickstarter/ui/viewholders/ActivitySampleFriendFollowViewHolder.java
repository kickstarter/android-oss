package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivitySampleFriendFollowViewHolder extends KSViewHolder {
  @Inject KSString ksString;

  protected @Bind(R.id.activity_image) ImageView activityImageView;
  protected @Bind(R.id.activity_title) TextView activityTitleTextView;
  protected @Bind(R.id.activity_subtitle) TextView activitySubtitleTextView;
  protected @Bind(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_user_name_is_now_following_you) String categoryFollowingString;
  protected @BindString(R.string.activity_follow_back) String categoryFollowBackString;

  protected Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void activitySampleFriendFollowViewHolderSeeActivityClicked(ActivitySampleFriendFollowViewHolder viewHolder);
  }

  public ActivitySampleFriendFollowViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
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
    final Context context = view.getContext();

    final User user = activity.user();
    if (user != null) {
      Picasso.with(context).load(user.avatar()
        .small())
        .transform(new CircleTransformation())
        .into(activityImageView);

      activityTitleTextView.setText(ksString.format(categoryFollowingString, "user_name", user.name()));
      activitySubtitleTextView.setText(categoryFollowBackString);

      // temp until followable :
      activitySubtitleTextView.setVisibility(View.GONE);
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    delegate.activitySampleFriendFollowViewHolderSeeActivityClicked(this);
  }
}
