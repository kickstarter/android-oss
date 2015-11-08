package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.activities.LoginToutActivity;
import com.kickstarter.ui.views.LoginPopupMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class LoginToutToolbar extends KSToolbar {
  @Bind(R.id.help_button) TextView helpButton;

  public LoginToutToolbar(@NonNull final Context context) {
    super(context);
  }

  public LoginToutToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public LoginToutToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.back_button)
  protected void backButtonClick() {
    ((LoginToutActivity) getContext()).onBackPressed();
  }

  @OnClick(R.id.help_button)
  protected void helpButtonClick() {
    new LoginPopupMenu(getContext(), helpButton).show();
  }
}

