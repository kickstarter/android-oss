package com.kickstarter.ui.viewholders;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
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
  @Bind(R.id.vertical_line_thin_view) View verticalLineView;
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

    setFont();

    if (style.primary()) {
      discoveryFilterView.setPadding(0, 5, 0, 10);
      verticalLineGroup.setVisibility(View.GONE);
    } else {
      discoveryFilterView.setPadding(0, 0, 0, 0);
      verticalLineGroup.setVisibility(View.VISIBLE);
    }

    if (style.visible()) {
      discoveryFilterView.setVisibility(View.VISIBLE);
      final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      discoveryFilterView.setLayoutParams(layoutParams);
    } else {
      discoveryFilterView.setVisibility(View.GONE);
      final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(0, 0);
      discoveryFilterView.setLayoutParams(layoutParams);
    }

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

  protected void setFont() {
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
}
