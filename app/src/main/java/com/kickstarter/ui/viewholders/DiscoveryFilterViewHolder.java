package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.DiscoveryUtils;
import com.kickstarter.libs.Font;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;
import com.kickstarter.ui.adapters.DiscoveryFilterAdapter;

import javax.inject.Inject;

import auto.parcel.AutoParcel;
import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

public class DiscoveryFilterViewHolder extends KsrViewHolder {
  private final Delegate delegate;
  @Inject Font font;
  private DiscoveryParams params;
  private DiscoveryFilterStyle style;

  @Bind(R.id.discovery_filter_view) View discoveryFilterView;
  @Bind(R.id.category_live_project_count_view) TextView categoryLiveProjectCountTextView;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.vertical_line_group) View verticalLineGroup;
  @Bind(R.id.vertical_line_medium_view) View verticalLineView;

  public interface Delegate {
    void discoveryFilterClick(final DiscoveryFilterViewHolder viewHolder, final DiscoveryParams discoveryParams);
  }

  public DiscoveryFilterViewHolder(@NonNull final View view, @NonNull final Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  public void onBind(@NonNull final Object datum) {
    final Filter filter = (Filter) datum;
    params = filter.params();
    style = filter.style();

    setCategoryLiveProjectCountTextView();
    setFilterTextView();
    setPadding();
    setVerticalLineStyle();
  }

  @Override
  public void onClick(@NonNull final View view) {
    delegate.discoveryFilterClick(this, params);
  }

  protected void setCategoryLiveProjectCountTextView() {
    categoryLiveProjectCountTextView.setTextColor(foregroundColor());

    if (style.showLiveProjectsCount()) {
      categoryLiveProjectCountTextView.setVisibility(View.VISIBLE);
      categoryLiveProjectCountTextView.setText(params.category().projectsCount().toString());
    } else {
      categoryLiveProjectCountTextView.setVisibility(View.GONE);
      categoryLiveProjectCountTextView.setText("");
    }
  }

  protected void setFilterTextView() {
    filterTextView.setTextColor(foregroundColor());

    if (style.selected()) {
      filterTextView.setTypeface(font.sansSerifTypeface());
    } else {
      filterTextView.setTypeface(font.sansSerifLightTypeface());
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

    String text = params.filterString(view.getContext());
    if (isSecondaryCategoryRoot()) {
      text = view.getContext().getString(R.string.All_of_Category, text);
    }

    filterTextView.setText(text);
  }

  protected void setPadding() {
    if (style.primary() && !style.selected()) {
      discoveryFilterView.setPadding(0, 5, 0, 10);
    } else {
      discoveryFilterView.setPadding(0, 0, 0, 0);
    }
  }

  protected void setVerticalLineStyle() {
    if (style.primary() && !style.selected()) {
      verticalLineGroup.setVisibility(View.GONE);
    } else {
      verticalLineGroup.setVisibility(View.VISIBLE);
    }

    verticalLineView.setBackgroundColor(foregroundColor());
  }

  protected int foregroundColor() {
    return DiscoveryUtils.overlayTextColor(view.getContext(), style.light());
  }

  protected boolean isSecondaryCategoryRoot() {
    return !style.primary() && params.category() != null && params.category().isRoot();
  }

  @AutoParcel
  public abstract static class Filter {
    public abstract DiscoveryParams params();
    public abstract DiscoveryFilterStyle style();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder params(DiscoveryParams __);
      public abstract Builder style(DiscoveryFilterStyle __);
      public abstract Filter build();
    }

    public static Builder builder() {
      return new AutoParcel_DiscoveryFilterViewHolder_Filter.Builder();
    }
  }
}
