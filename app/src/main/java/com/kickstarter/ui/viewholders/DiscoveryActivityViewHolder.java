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

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
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

public class DiscoveryActivityViewHolder extends KSViewHolder {
  @Inject KSString ksString;

  protected @Bind(R.id.activity_click_area) LinearLayout activityClickArea;
  protected @Bind(R.id.activity_image) ImageView activityImageView;
  protected @Bind(R.id.activity_title) TextView activityTitleTextView;
  protected @Bind(R.id.activity_subtitle) TextView activitysubTitleTextView;
  protected @Bind(R.id.see_activity_button) Button seeActivityButton;
  protected @BindString(R.string.activity_friend_backed_project_name_by_creator_name) String categoryBackingString;
  protected @BindString(R.string.activity_user_name_is_now_following_you) String categoryFollowingString;
  protected @BindString(R.string.activity_follow_back) String categoryFollowBackString;
  protected @BindString(R.string.activity_project_was_not_successfully_funded) String categoryFailureString;
  protected @BindString(R.string.activity_user_name_launched_project) String categoryLaunchString;
  protected @BindString(R.string.activity_successfully_funded) String categorySuccessString;
  protected @BindString(R.string.activity_funding_canceled) String categoryCancellationString;
  protected @BindString(R.string.activity_posted_update_number_title) String categoryUpdateString;

  protected Activity activity;

  private final Delegate delegate;
  public interface Delegate {
    void discoveryActivityViewHolderSeeActivityClicked(DiscoveryActivityViewHolder viewHolder);
    void discoveryActivityViewHolderProjectClicked(DiscoveryActivityViewHolder viewHolder, Project project);
    void discoveryActivityViewHolderUpdateClicked(DiscoveryActivityViewHolder viewHolder, Activity activity);
  }

  public DiscoveryActivityViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    activity = ObjectUtils.requireNonNull((Activity)data, Activity.class);
  }

  public void onBind() {
    final Context context = view.getContext();

    activityTitleTextView.setVisibility(View.GONE);
    activitysubTitleTextView.setVisibility(View.GONE);
    activityImageView.setVisibility(View.GONE);

    final User user = activity.user();
    final Project project = activity.project();

    if (activity.category().equals(Activity.CATEGORY_BACKING)) {
      if (user == null || project == null) {
        return;
      }
      setBackingView(context, user, project);

    } else if (activity.category().equals(Activity.CATEGORY_FOLLOW)) {
      if (user == null) {
        return;
      }
      setFollowView(context, user);

    } else {
      if (project == null) {
        return;
      }
      setProjectView(context, project, user);
    }
  }

  private void setBackingView(final @NonNull Context context, final @NonNull User user, final @NonNull Project project) {
    activityImageView.setVisibility(View.VISIBLE);
    activitysubTitleTextView.setVisibility(View.VISIBLE);

    Picasso.with(context).load(user.avatar()
      .small())
      .transform(new CircleTransformation())
      .into(activityImageView);

    activitysubTitleTextView.setText(Html.fromHtml(ksString.format(categoryBackingString,
      "friend_name", user.name(),
      "project_name", project.name(),
      "creator_name", project.creator().name())));
  }

  private void setFollowView(final @NonNull Context context, final @NonNull User user) {
    activityImageView.setVisibility(View.VISIBLE);
    activityTitleTextView.setVisibility(View.VISIBLE);
    activitysubTitleTextView.setVisibility(View.VISIBLE);

    Picasso.with(context).load(user.avatar()
      .small())
      .transform(new CircleTransformation())
      .into(activityImageView);

    activityTitleTextView.setText(ksString.format(categoryFollowingString, "user_name", user.name()));
    activitysubTitleTextView.setText(categoryFollowBackString);

    // temp until followable :
    activityClickArea.setBackgroundResource(0);
    activitysubTitleTextView.setVisibility(View.GONE);
  }

  private void setProjectView(final @NonNull Context context, final @NonNull Project project, final @Nullable User user) {
    final Photo photo = project.photo();
    if (photo != null) {
      Picasso.with(context)
        .load(photo.little())
        .into(activityImageView);
    }

    activityImageView.setVisibility(View.VISIBLE);
    activityTitleTextView.setVisibility(View.VISIBLE);
    activitysubTitleTextView.setVisibility(View.VISIBLE);

    final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(context.getResources().getDimensionPixelSize(R.dimen.discovery_activity_photo_width),
      context.getResources().getDimensionPixelSize(R.dimen.discovery_activity_photo_height));
    activityImageView.setLayoutParams(layoutParams);

    activityTitleTextView.setText(project.name());

    switch(activity.category()) {
      case Activity.CATEGORY_FAILURE:
        activitysubTitleTextView.setText(categoryFailureString);
        break;
      case Activity.CATEGORY_CANCELLATION:
        activitysubTitleTextView.setText(categoryCancellationString);
        break;
      case Activity.CATEGORY_LAUNCH:
        if (user == null) {
          break;
        }
        activitysubTitleTextView.setText(ksString.format(categoryLaunchString, "user_name", user.name()));
        break;
      case Activity.CATEGORY_SUCCESS:
        activitysubTitleTextView.setText(categorySuccessString);
        break;
      case Activity.CATEGORY_UPDATE:
        final Update update = activity.update();
        if (update == null) {
          break;
        }
        activitysubTitleTextView.setText(ksString.format(categoryUpdateString,
          "update_number", String.valueOf(update.sequence()),
          "update_title", update.title()));
        break;
      default:
        break;
    }
  }

  @OnClick(R.id.see_activity_button)
  protected void seeActivityOnClick() {
    delegate.discoveryActivityViewHolderSeeActivityClicked(this);
  }

  @OnClick(R.id.activity_click_area)
  protected void activityProjectOnClick() {
    if (activity.category().equals(Activity.CATEGORY_UPDATE)) {
      delegate.discoveryActivityViewHolderUpdateClicked(this, activity);
    } else if(activity.category().equals(Activity.CATEGORY_FOLLOW)) {
      // TODO: HOLLA BACK GIRL
    } else {
      delegate.discoveryActivityViewHolderProjectClicked(this, activity.project());
    }
  }
}
