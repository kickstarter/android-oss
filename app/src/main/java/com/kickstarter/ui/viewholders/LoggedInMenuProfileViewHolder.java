package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class LoggedInMenuProfileViewHolder extends KSViewHolder {
  public @Bind(R.id.menu_item_title) TextView menuItemTitleTextView;
  public @Bind(R.id.avatar) ImageView avatarImageView;

  private final Context context;
  private final Delegate delegate;

  public interface Delegate {
    void menuItemClicked(LoggedInMenuProfileViewHolder viewHolder, String title);
  }

  public LoggedInMenuProfileViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    this.context = view.getContext();

    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    Timber.d("data");
    //menuItemTitleTextView.setText(helpString);
  }

  @Override
  public void onClick(@NonNull final View view) {
    //delegate.menuItemClicked(this, menuItemTitleTextView.getText().toString());
  }
}
