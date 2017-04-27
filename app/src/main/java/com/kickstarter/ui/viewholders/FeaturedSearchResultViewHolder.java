package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;

import com.kickstarter.libs.RefTag;

public final class FeaturedSearchResultViewHolder extends ProjectSearchResultViewHolder {
  public FeaturedSearchResultViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view, delegate);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    super.bindData(data);
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.projectSearchResultClick(this, project, new Pair<>(RefTag.searchFeatured(), RefTag.searchPopularFeatured()));
  }
}
