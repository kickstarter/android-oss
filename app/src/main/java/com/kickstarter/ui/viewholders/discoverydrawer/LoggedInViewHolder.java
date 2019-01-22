package com.kickstarter.ui.viewholders.discoverydrawer;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.libs.transformations.CircleTransformation;
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

  protected @Bind(R.id.user_image_view) ImageView userImageView;
  protected @Bind(R.id.user_name_text_view) TextView userNameTextView;

  public interface Delegate {
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
    Picasso.with(context)
      .load(this.user.avatar().medium())
      .transform(new CircleTransformation())
      .into(this.userImageView);
  }

  @OnClick(R.id.user_container)
  public void userClick() {
    this.delegate.loggedInViewHolderProfileClick(this, this.user);
  }

  @Nullable @OnClick(R.id.internal_tools_icon_button)
  public void internalToolsClick() {
    this.delegate.loggedInViewHolderInternalToolsClick(this);
  }

  @OnClick(R.id.settings_icon_button)
  public void settingsClick() {
    this.delegate.loggedInViewHolderSettingsClick(this, this.user);
  }
}
