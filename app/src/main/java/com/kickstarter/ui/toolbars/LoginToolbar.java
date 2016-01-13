package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.views.LoginPopupMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginToolbar extends KSToolbar {
  @Bind(R.id.help_button) TextView helpButton;

  public LoginToolbar(@NonNull final Context context) {
    super(context);
  }

  public LoginToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public LoginToolbar(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.help_button)
  protected void helpButtonClick() {
    new LoginPopupMenu(getContext(), helpButton).show();
  }
}
