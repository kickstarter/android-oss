package com.kickstarter.ui.viewholders;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Font;
import com.kickstarter.libs.KSString;
import com.kickstarter.libs.utils.DiscoveryUtils;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.DiscoveryFilterStyle;

import javax.inject.Inject;

import auto.parcel.AutoParcel;
import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;

import static com.kickstarter.libs.utils.ObjectUtils.requireNonNull;

public final class DiscoveryFilterViewHolder extends KSViewHolder {
  private final Delegate delegate;
  private DiscoveryParams params;
  private DiscoveryFilterStyle style;

  protected @Bind(R.id.discovery_filter_view) View discoveryFilterView;
  protected @Bind(R.id.category_live_project_count_view) TextView categoryLiveProjectCountTextView;
  protected @Bind(R.id.filter_text_view) TextView filterTextView;
  protected @Bind(R.id.text_group) RelativeLayout textGroupLayout;
  protected @Bind(R.id.vertical_line_group) View verticalLineGroup;
  protected @Bind(R.id.vertical_line_medium_view) View verticalLineView;

  protected @BindString(R.string.discovery_all_of_scope) String allOfScopeString;
  protected @BindString(R.string.discovery_accessibility_live_project_count) String liveProjectCountDescriptionString;

  @Inject Font font;
  @Inject KSString ksString;

  public interface Delegate {
    void discoveryFilterClick(DiscoveryFilterViewHolder viewHolder, DiscoveryParams discoveryParams);
  }

  public DiscoveryFilterViewHolder(final @NonNull View view, final @NonNull Delegate delegate) {
    super(view);
    this.delegate = delegate;
    ButterKnife.bind(this, view);
    ((KSApplication) view.getContext().getApplicationContext()).component().inject(this);
  }

  @Override
  public void bindData(final @Nullable Object data) throws Exception {
    final Filter filter = requireNonNull((Filter) data);
    params = requireNonNull(filter.params(), DiscoveryParams.class);
    style = requireNonNull(filter.style(), DiscoveryFilterStyle.class);
  }

  public void onBind() {
    setCategoryLiveProjectCountTextView();
    setFilterTextView();
    setViewSpacing();
    setTextGroupLayoutSpacing();
    setVerticalLineStyle();
  }

  @Override
  public void onClick(final @NonNull View view) {
    delegate.discoveryFilterClick(this, params);
  }

  protected void setCategoryLiveProjectCountTextView() {
    categoryLiveProjectCountTextView.setTextColor(foregroundColor());

    if (style.showLiveProjectsCount()) {
      categoryLiveProjectCountTextView.setVisibility(View.VISIBLE);
      categoryLiveProjectCountTextView.setText(params.category().projectsCount().toString());
      categoryLiveProjectCountTextView.setContentDescription(params.category().projectsCount() + liveProjectCountDescriptionString);
    } else {
      categoryLiveProjectCountTextView.setVisibility(View.GONE);
      categoryLiveProjectCountTextView.setText("");
      categoryLiveProjectCountTextView.setContentDescription("");
    }
  }

  protected void setFilterTextView() {
    filterTextView.setTextColor(foregroundColor());

    if (style.selected() && !style.primary()) {
      filterTextView.setTypeface(font.sansSerifTypeface());
    } else {
      filterTextView.setTypeface(font.sansSerifLightTypeface());
    }

    if (style.primary()) {
      filterTextView.setTextSize(20.0f);
    } else {
      filterTextView.setTextSize(18.0f);
    }

    if (!style.selected() && !style.primary()) {
      filterTextView.setAlpha(0.8f);
    } else {
      filterTextView.setAlpha(1.0f);
    }

    String text = params.filterString(view.getContext());
    if (isSecondaryCategoryRoot()) {
      text = ksString.format(allOfScopeString, "scope", text);
    }

    filterTextView.setText(text);
  }

  protected void setViewSpacing() {
    final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT);
    if (style.primary() && style.selected()) {
      params.setMargins(0, 36, 0, 0);
    } else {
      params.setMargins(0, 0, 0, 0);
    }
    discoveryFilterView.setLayoutParams(params);
  }

  protected void setTextGroupLayoutSpacing() {
    final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT);
    if (style.primary() && style.selected()) {
      params.setMargins(0, 0, 0, 12);
    } else if (style.primary() && !style.selected()) {
      params.setMargins(0, 20, 0, 20);
    } else {
      params.setMargins(0, 12, 0, 12);
    }
    textGroupLayout.setLayoutParams(params);
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
