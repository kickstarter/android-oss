package com.kickstarter.ui.viewholders;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.HamburgerNavigationItem;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class HamburgerNavigationParentFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  private HamburgerNavigationItem item;

  public HamburgerNavigationParentFilterViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    this.item = (HamburgerNavigationItem) datum;
    final Context context = view.getContext();

    filterTextView.setText(item.discoveryParams().filterString(context));
  }
}

