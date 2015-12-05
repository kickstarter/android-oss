package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

public class LoggedInMenuViewHolder extends KSViewHolder {
  public @Bind(R.id.menu_item_title) TextView menuItemTitleTextView;
  @BindString(R.string.___Help) String helpString;
  @BindString(R.string.___Settings) String settingsString;
  @BindString(R.string.___Find_friends) String findFriendsString;

  private final Context context;
  private final Delegate delegate;

  public interface Delegate {
    void menuItemClicked(LoggedInMenuViewHolder viewHolder, String title);
  }

  public LoggedInMenuViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    menuItemTitleTextView.setText(helpString);
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.menuItemClicked(this, menuItemTitleTextView.getText().toString());
  }
}
