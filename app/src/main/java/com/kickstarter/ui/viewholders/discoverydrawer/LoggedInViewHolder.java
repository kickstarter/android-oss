package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
import com.kickstarter.libs.utils.IntegerUtils;
import com.kickstarter.libs.utils.ViewUtils;
import com.kickstarter.models.User;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class LoggedInViewHolder extends KSViewHolder {
  private Delegate delegate;
  private User user;

  protected @Bind(R.id.drawer_dashboard) View creatorDashboardTextView;
  protected @Bind(R.id.user_image_view) ImageView userImageView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;

  public interface Delegate {
    void loggedInViewHolderActivityClick(final @NonNull LoggedInViewHolder viewHolder);
    void loggedInViewHolderDashboardClick(final @NonNull LoggedInViewHolder viewHolder);
    void loggedInViewHolderInternalToolsClick(final @NonNull LoggedInViewHolder viewHolder);
    void loggedInViewHolderProfileClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user);
    void loggedInViewHolderSettingsClick(final @NonNull LoggedInViewHolder viewHolder, final @NonNull User user);
  }

  public LoggedInViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    this.user = requireNonNull((User) data, User.class);
  }

  @Override
  public void onBind() {
    final Context context = context();

    this.userNameTextView.setText(this.user.name());

    ViewUtils.setGone(this.creatorDashboardTextView, IntegerUtils.isZero(this.user.memberProjectsCount()));

    Picasso.with(context)
      .load(this.user.avatar().medium())
      .transform(new CircleTransformation())
      .into(this.userImageView);
  }

  @OnClick(R.id.drawer_activity)
  public void activityClick() {
    this.delegate.loggedInViewHolderActivityClick(this);
  }

  @OnClick(R.id.drawer_dashboard)
  public void dashboardClick() {
    this.delegate.loggedInViewHolderDashboardClick(this);
  }

  @Nullable @OnClick(R.id.internal_tools)
  public void internalToolsClick() {
    this.delegate.loggedInViewHolderInternalToolsClick(this);
  }

  @OnClick(R.id.drawer_settings)
  public void settingsClick() {
    this.delegate.loggedInViewHolderSettingsClick(this, this.user);
  }

  @OnClick({R.id.user_container, R.id.drawer_profile})
  public void userClick() {
    this.delegate.loggedInViewHolderProfileClick(this, this.user);
  }
}
