package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class LoggedInViewHolder extends KSViewHolder {
  private User user;

  protected @Bind(R.id.user_image_view) ImageView userImageView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;

  public LoggedInViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    user = requireNonNull((User) data, User.class);
  }

  @Override
  public void onBind() {
    final Context context = view.getContext();

    userNameTextView.setText(user.name());
    Picasso.with(context)
      .load(user.avatar().medium())
      .transform(new CircleTransformation())
      .into(userImageView);
  }

  @OnClick(R.id.user_container)
  public void userClick() {
  }

  @OnClick(R.id.settings_icon_button)
  public void settingsClick() {
  }
}
