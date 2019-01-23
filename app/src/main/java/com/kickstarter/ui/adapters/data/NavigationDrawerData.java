package com.kickstarter.ui.adapters.data;

import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoParcel
public abstract class NavigationDrawerData {
  public abstract @Nullable User user();
  public abstract List<Section> sections();

  public abstract @Nullable Category expandedCategory();
  public abstract @Nullable DiscoveryParams selectedParams();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder user(User __);
    public abstract Builder sections(List<Section> __);
    public abstract Builder expandedCategory(Category __);
    public abstract Builder selectedParams(DiscoveryParams __);
    public abstract NavigationDrawerData build();
  }
  public static Builder builder() {
    return new AutoParcel_NavigationDrawerData.Builder()
      .user(null)
      .expandedCategory(null)
      .selectedParams(null)
      .sections(ListUtils.empty());
  }
  public abstract Builder toBuilder();

  @AutoParcel
  static public abstract class Section {
    public abstract boolean expandable();
    public abstract boolean expanded();
    public abstract List<Section.Row> rows();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Section.Builder expandable(boolean __);
      public abstract Section.Builder expanded(boolean __);
      public abstract Section.Builder rows(List<Section.Row> __);
      public abstract Section build();
    }

    public static Section.Builder builder() {
      return new AutoParcel_NavigationDrawerData_Section.Builder()
        .expandable(false)
        .expanded(false)
        .rows(ListUtils.empty());
    }
    public abstract Section.Builder toBuilder();

    public boolean isCategoryFilter() {
      return rows().size() >= 1 && rows().get(0).params().isCategorySet();
    }

    public boolean isTopFilter() {
      return !isCategoryFilter();
    }

    @AutoParcel
    static public abstract class Row {
      public abstract @NonNull DiscoveryParams params();
      public abstract boolean selected();
      public abstract boolean rootIsExpanded();

      @AutoParcel.Builder
      public static abstract class Builder {
        public abstract Builder params(DiscoveryParams __);
        public abstract Builder selected(boolean __);
        public abstract Builder rootIsExpanded(boolean __);
        public abstract Section.Row build();
      }
      public static Builder builder() {
        return new AutoParcel_NavigationDrawerData_Section_Row.Builder()
          .params(DiscoveryParams.builder().build())
          .selected(false)
          .rootIsExpanded(false);
      }
      public abstract Section.Row.Builder toBuilder();
    }
  }
}
