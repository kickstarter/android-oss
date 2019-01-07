package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public class ProjectSocialViewHolder extends KSViewHolder {
  private User user;
  protected @Bind(R.id.friend_image) ImageView friendImageView;
  protected @Bind(R.id.friend_name) TextView friendNameTextView;

  public ProjectSocialViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.user = requireNonNull((User) data, User.class);
  }

  @Override
  public void onBind() {
    Picasso.with(context()).load(this.user.avatar().small())
    .transform(new CircleTransformation())
    .into(this.friendImageView);

    this.friendNameTextView.setText(this.user.name());
  }
}
