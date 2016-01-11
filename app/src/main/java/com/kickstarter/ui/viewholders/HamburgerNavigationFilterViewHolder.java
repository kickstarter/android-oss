package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;

import butterknife.Bind;
import butterknife.ButterKnife;

public final class HamburgerNavigationFilterViewHolder extends KSViewHolder {
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  private DiscoveryParams params;

  public HamburgerNavigationFilterViewHolder(final @NonNull View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  @Override
  public void onBind(final @NonNull Object datum) {
    this.params = (DiscoveryParams) datum;
    final Context context = view.getContext();

    filterTextView.setText(params.filterString(context));
  }
}

