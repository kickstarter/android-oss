package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class DiscoveryOnboardingViewHolder extends KSViewHolder{
  protected @Bind(R.id.signup_login_button) TextView signupLoginButton;

  private final Delegate delegate;
  public interface Delegate {
    void signupLoginClick(DiscoveryOnboardingViewHolder viewHolder);
  }

  public DiscoveryOnboardingViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
  }
}
