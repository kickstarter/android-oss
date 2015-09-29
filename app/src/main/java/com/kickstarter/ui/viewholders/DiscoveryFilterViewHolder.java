package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.R;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;
import com.kickstarter.ui.adapters.DiscoveryFilterAdapter;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  private DiscoveryParams params;
  private DiscoveryFilterStyle style;

  @Bind(R.id.discovery_filter_view) View discoveryFilterView;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.vertical_line_group) View verticalLineGroup;
  @Bind(R.id.vertical_line_medium_view) View verticalLineView;
  @BindColor(R.color.white) int whiteColor;

  public interface Delegate {
    void discoveryFilterClick(final DiscoveryFilterViewHolder viewHolder, final DiscoveryParams discoveryParams);
  }

  public DiscoveryFilterViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
  }

  public void onBind(@NonNull final Object datum) {
    final DiscoveryFilterAdapter.Filter filter = (DiscoveryFilterAdapter.Filter) datum;
    params = filter.params();
    style = filter.style();
    final Context context = view.getContext();

    setFilterTextViewStyle();
    setPadding();
    setVerticalLineStyle();

    String text = params.filterString(view.getContext());
    if (isNestedRoot()) {
      text = context.getString(R.string.All_of_Category, text);
    }

    filterTextView.setText(text);
    verticalLineView.setBackgroundColor(whiteColor);
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.discoveryFilterClick(this, params);
  }

  protected boolean isNestedRoot() {
    return !style.primary() && params.category() != null && params.category().isRoot();
  }

  protected void setFilterTextViewStyle() {
    // TODO: Cache fonts
    if (style.selected()) {
      filterTextView.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    } else {
      filterTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    if (style.primary()) {
      filterTextView.setTextSize(18.0f);
    } else {
      filterTextView.setTextSize(16.0f);
    }

    if (!style.selected() && !style.primary()) {
      filterTextView.setAlpha(0.8f);
    } else {
      filterTextView.setAlpha(1.0f);
    }
  }

  protected void setVerticalLineStyle() {
    if (style.primary() && !style.selected()) {
      verticalLineGroup.setVisibility(View.GONE);
    } else {
      verticalLineGroup.setVisibility(View.VISIBLE);
    }
  }

  protected void setPadding() {
    if (style.primary() && !style.selected()) {
      discoveryFilterView.setPadding(0, 5, 0, 10);
    } else {
      discoveryFilterView.setPadding(0, 0, 0, 0);
    }
  }
}
