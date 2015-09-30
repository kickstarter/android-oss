package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import com.kickstarter.R;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterDividerViewHolder extends KsrViewHolder {
  @Bind(R.id.horizontal_line_thin_view) View horizontalLineView;
  @BindColor(R.color.white) int whiteColor;

  public DiscoveryFilterDividerViewHolder(@NonNull final View view) {
    super(view);
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    horizontalLineView.setBackgroundColor(whiteColor);
  }
}
