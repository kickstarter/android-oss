package com.kickstarter.ui.toolbars;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.ui.views.LoginPopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginToolbar extends KSToolbar {
  @Bind(R.id.help_button) TextView helpButton;

  public LoginToolbar(final @NonNull Context context) {
    super(context);
  }

  public LoginToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public LoginToolbar(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);
  }

  @OnClick(R.id.help_button)
  protected void helpButtonClick() {
    new LoginPopupMenu(getContext(), this.helpButton).show();
  }
}
