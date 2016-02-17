package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.SocialUtils;
import com.kickstarter.models.Activity;
import com.kickstarter.models.Category;
import com.kickstarter.models.Photo;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class FriendBackingViewHolder extends ActivityListViewHolder {
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.creator_name) TextView creatorNameTextView;
  protected @Bind(R.id.project_name) TextView projectNameTextView;
  protected @Bind(R.id.project_photo) ImageView projectPhotoImageView;
  protected @Bind(R.id.title) TextView titleTextView;

  protected @BindString(R.string.project_creator_by_creator) String projectByCreatorString;

  @Inject KSString ksString;

  private final Delegate delegate;

  public interface Delegate {
    void friendBackingClicked(FriendBackingViewHolder viewHolder, Activity activity);
  }

  public FriendBackingViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind() {
    final Context context = context();

    final User activityUser = activity().user();
    if (activityUser == null) {
      return;
    }
    final Project activityProject = activity().project();
    if (activityProject == null) {
      return;
    }
    final User projectCreator = activityProject.creator();
    if (projectCreator == null) {
      return;
    }
    final Category projectCategory = activityProject.category();
    if (projectCategory == null) {
      return;
    }
    final Photo projectPhoto = activityProject.photo();
    if (projectPhoto == null) {
      return;
    }

    Picasso.with(context)
      .load(activityUser.avatar().small())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    creatorNameTextView.setText(ksString.format(
      projectByCreatorString,
      "creator_name",
      projectCreator.name()
    ));

    projectNameTextView.setText(activityProject.name());

    Picasso.with(context)
      .load(projectPhoto.little())
      .into(projectPhotoImageView);

    titleTextView.setText(SocialUtils.friendBackingActivityTitle(context,
      activityUser.name(),
      projectCategory.rootId(),
      ksString
    ));
  }

  @OnClick(R.id.friend_backing_card_view)
  public void onClick() {
    delegate.friendBackingClicked(this, activity());
  }
}

