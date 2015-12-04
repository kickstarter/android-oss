package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public final class FriendFollowViewHolder extends ActivityListViewHolder {
  @Bind(R.id.avatar) ImageView avatarImageView;
  @Bind(R.id.follow_button) View followButton;
  @Bind(R.id.title) TextView titleTextView;
  @BindString(R.string.___Not_implemented_yet) String notImplementedYetString;

  public FriendFollowViewHolder(@NonNull final View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(@NonNull final Object datum) {
    super.onBind(datum);

    final Context context = view.getContext();

    Picasso.with(context)
      .load(activity.user().avatar().small())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    titleTextView.setText(Html.fromHtml(context.getString(R.string.___username_is_following_you, activity.user().name())));
  }

  @Override
  public void onClick(@NonNull final View view) {
    Toast.makeText(view.getContext(), notImplementedYetString, Toast.LENGTH_LONG).show();
  }
}
