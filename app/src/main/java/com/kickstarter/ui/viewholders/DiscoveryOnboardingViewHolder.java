package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class DiscoveryOnboardingViewHolder extends KSViewHolder{
  protected @Bind(R.id.signup_login_button) Button signupLoginButton;

  private final Delegate delegate;
  public interface Delegate {
    void discoveryOnboardingViewHolderSignupLoginClicked(DiscoveryOnboardingViewHolder viewHolder);
  }

  public DiscoveryOnboardingViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;

    ButterKnife.bind(this, view);
  }

  public void onBind(final @NonNull Object datum) {}

  @OnClick(R.id.signup_login_button)
  protected void signupLoginOnClick() {
    delegate.discoveryOnboardingViewHolderSignupLoginClicked(this);
  }
}
