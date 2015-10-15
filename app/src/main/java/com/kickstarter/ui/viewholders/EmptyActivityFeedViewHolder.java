package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class EmptyActivityFeedViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  @Inject CurrentUser currentUser;

  public interface Delegate {
    void emptyActivityFeedDiscoverProjectsClicked(@NonNull EmptyActivityFeedViewHolder viewHolder);
  }

  public EmptyActivityFeedViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void onBind(@NonNull Object datum) {
    Log.d("TEST", "binding emptyActivityFeed...");
  }

  @OnClick(R.id.discover_projects_button)
  public void discoverProjectsOnClick() {
    delegate.emptyActivityFeedDiscoverProjectsClicked(this);
  }
}
