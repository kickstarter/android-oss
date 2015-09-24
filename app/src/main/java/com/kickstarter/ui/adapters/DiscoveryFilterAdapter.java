package com.kickstarter.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.R;
import com.kickstarter.models.Category;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.viewholders.DiscoveryFilterViewHolder;
import com.kickstarter.ui.viewholders.KsrViewHolder;

import java.util.List;

public class DiscoveryFilterAdapter extends KsrAdapter {
  private List<DiscoveryParams> discoveryParams;
  private final Delegate delegate;

  public interface Delegate extends DiscoveryFilterViewHolder.Delegate {}

  public DiscoveryFilterAdapter(final List<DiscoveryParams> discoveryParams, final Delegate delegate) {
    data().add(discoveryParams);
    this.delegate = delegate;
  }

  protected int layout(final SectionRow sectionRow) {
    return R.layout.discovery_filter_view;
  }

  protected KsrViewHolder viewHolder(final int layout, final View view) {
    return new DiscoveryFilterViewHolder(view, delegate);

  }
}
