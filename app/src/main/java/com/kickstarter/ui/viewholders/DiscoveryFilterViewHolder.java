package com.kickstarter.ui.viewholders;

import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends KsrViewHolder {
  private DiscoveryParams discoveryParams;
  private final Delegate delegate;
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
    discoveryParams = (DiscoveryParams) datum;

    verticalLineGroup.setVisibility(View.GONE);

    if (discoveryParams.category() != null) {
      final Category category = discoveryParams.category();
      if (!category.isRoot()) {
        verticalLineGroup.setVisibility(View.VISIBLE);
      }
    }

    filterTextView.setText(discoveryParams.filterString(view.getContext()));

    verticalLineView.setBackgroundColor(whiteColor);
  }

  @Override
  public void onClick(final View view) {
    delegate.discoveryFilterClick(this, discoveryParams);
  }
}
