package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.models.User;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HamburgerNavigationHeader extends LinearLayout implements HamburgerNavigationHeaderType {
  protected @Bind(R.id.logged_in_container) View loggedInContainer;
  protected @Bind(R.id.logged_out_container) View loggedOutContainer;
  protected @Bind(R.id.user_image_view) ImageView userImageView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;

  protected @Inject CurrentUser currentUser;
  private User user;

  public HamburgerNavigationHeader(final @NonNull Context context) {
    this(context, null);
  }

  public HamburgerNavigationHeader(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public HamburgerNavigationHeader(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public HamburgerNavigationHeader(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);
    ButterKnife.bind(this);
  }

  @OnClick(R.id.user_container)
  public void userClick() {
  }

  @OnClick(R.id.logged_out_container)
  public void loggedOutClick() {
  }

  @OnClick(R.id.settings_icon_button)
  public void settingsClick() {
  }

  @Override
  public void user(final @Nullable User user) {
    this.user = user;
    final Context context = getContext();

    if (user != null) {
      loggedInContainer.setVisibility(VISIBLE);
      loggedOutContainer.setVisibility(GONE);
      userNameTextView.setText(user.name());
      Picasso.with(context)
        .load(user.avatar().medium())
        .transform(new CircleTransformation())
        .into(userImageView);
    } else {
      loggedInContainer.setVisibility(GONE);
      loggedOutContainer.setVisibility(VISIBLE);
    }
  }
}
