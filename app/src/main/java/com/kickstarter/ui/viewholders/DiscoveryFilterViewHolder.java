package com.kickstarter.ui.viewholders;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  private DiscoveryParams discoveryParams;
  private DiscoveryFilterStyle style;

  @Bind(R.id.discovery_filter_view) View discoveryFilterView;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.vertical_line_group) View verticalLineGroup;
  @Bind(R.id.vertical_line_view_thick) View verticalLineView;
  @BindColor(R.color.white) int whiteColor;

  public interface Delegate {
    void discoveryFilterClick(final DiscoveryFilterViewHolder viewHolder, final DiscoveryParams discoveryParams);
  }

  public DiscoveryFilterViewHolder(final View view, final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(final Object datum) {
    final Pair<DiscoveryParams, DiscoveryFilterStyle> pair = (Pair<DiscoveryParams, DiscoveryFilterStyle>) datum;
    discoveryParams = pair.first;
    style = pair.second;

    delegate.discoveryFilterConfigure(this, discoveryParams);

    verticalLineView.setBackgroundColor(whiteColor);
    if (isSubcategory()) {
      verticalLineGroup.setVisibility(View.VISIBLE);
      discoveryFilterView.setPadding(0, 0, 0, 0);
    } else {
      verticalLineGroup.setVisibility(View.GONE);
      discoveryFilterView.setPadding(0, 5, 0, 10);
    }

    filterTextView.setText(discoveryParams.filterString(view.getContext()));
  }

  @Override
  public void onClick(final View view) {
    delegate.discoveryFilterClick(this, discoveryParams);
  }

  private boolean isSubcategory() {
    return discoveryParams.category() != null && !discoveryParams.category().isRoot();
  }
}
