package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.ObjectUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivitySampleFriendBackingViewHolder extends KSViewHolder {
  private final KSString ksString;

  protected @BindView(R.id.activity_click_area) LinearLayout activityClickArea;
  protected @BindView(R.id.activity_image) ImageView activityImageView;
  protected @BindView(R.id.activity_title) TextView activityTitleTextView;
  protected @BindView(R.id.activity_subtitle) TextView activitySubtitleTextView;
  protected @BindView(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_friend_backed_project_name_by_creator_name) String categoryBackingString;

  private Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void activitySampleFriendBackingViewHolderSeeActivityClicked(ActivitySampleFriendBackingViewHolder viewHolder);
    void activitySampleFriendBackingViewHolderProjectClicked(ActivitySampleFriendBackingViewHolder viewHolder, Project project);
  }

  public ActivitySampleFriendBackingViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
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

    final User user = this.activity.user();
    final Project project = this.activity.project();

    if (user != null && project != null) {
      this.activityTitleTextView.setVisibility(View.GONE);

      Picasso.with(context).load(user.avatar()
        .small())
        .transform(new CircleTransformation())
        .into(this.activityImageView);

      this.activitySubtitleTextView.setText(
        Html.fromHtml(
          this.ksString.format(
            this.categoryBackingString,
            "friend_name", user.name(),
            "project_name", project.name(),
            "creator_name", project.creator().name()
          )
        )
      );
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    this.delegate.activitySampleFriendBackingViewHolderSeeActivityClicked(this);
  }

  @OnClick(R.id.activity_click_area)
  protected void activityProjectOnClick() {
    this.delegate.activitySampleFriendBackingViewHolderProjectClicked(this, this.activity.project());
  }
}
