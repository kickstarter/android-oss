package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.kickstarter.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class EmptyProfileViewHolder extends KSViewHolder {
  protected @BindView(R.id.explore_projects_button) Button exploreButton;

  private final Delegate delegate;

  public interface Delegate {
    void emptyProfileViewHolderExploreProjectsClicked(EmptyProfileViewHolder viewHolder);
  }

  public EmptyProfileViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {}

  @Override
  public void onBind() {}

  @OnClick(R.id.explore_projects_button)
  public void exploreProjectsClicked() {
    this.delegate.emptyProfileViewHolderExploreProjectsClicked(this);
  }
}
