package com.kickstarter.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.qualifiers.RequiresPresenter;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.presenters.ProfilePresenter;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

@RequiresPresenter(ProfilePresenter.class)
public final class ProfileActivity extends BaseActivity<ProfilePresenter> {
  protected @Bind(R.id.avatar) ImageView avatarImageView;
  protected @Bind(R.id.user_name) TextView userNameTextView;
  protected @Bind(R.id.created_num) TextView createdNumTextView;
  protected @Bind(R.id.backed_num) TextView backedNumTextView;
  protected @Bind(R.id.followers_num) TextView followersNumTextView;

  @Inject CurrentUser currentUser;

  @Override
  protected void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    Picasso.with(this).load(currentUser.getUser().avatar()
      .medium())
      .transform(new CircleTransformation())
      .into(avatarImageView);

    userNameTextView.setText(currentUser.getUser().name());
    //createdNumTextView.setText(currentUser.getUser().launchedProjectsCount().toString());
    //backedNumTextView.setText(currentUser.getUser().backedProjectsCount().toString());
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();

    overridePendingTransition(R.anim.fade_in_slide_in_left, R.anim.slide_out_right);
  }
}
