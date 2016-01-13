package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.kickstarter.R;
import com.kickstarter.libs.utils.ListUtils;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.viewholders.EmptyViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationChildFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedInViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationHeaderLoggedOutViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationRootFilterViewHolder;
import com.kickstarter.ui.viewholders.HamburgerNavigationTopFilterViewHolder;
import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.Collections;
import java.util.List;

import auto.parcel.AutoParcel;

public class NavigationDrawerAdapter extends KSAdapter {

  private @NonNull Delegate delegate;

  public NavigationDrawerAdapter(final @NonNull Delegate delegate) {
    this.delegate = delegate;
  }

  public interface Delegate extends HamburgerNavigationTopFilterViewHolder.Delegate,
    HamburgerNavigationRootFilterViewHolder.Delegate, HamburgerNavigationChildFilterViewHolder.Delegate {}

  @Override
  protected int layout(@NonNull SectionRow sectionRow) {
    final Object object = objectFromSectionRow(sectionRow);
    switch (sectionRow.section()) {
      case 0:
        return (object == null) ?
          R.layout.hamburger_navigation_header_logged_out_view :
          R.layout.hamburger_navigation_header_logged_in_view;
      default:
        return layoutForDatum(object, sectionRow);
    }
  }

  private int layoutForDatum(Object datum, SectionRow sectionRow) {
    if (datum instanceof Data.Section.Row) {
      Data.Section.Row row = (Data.Section.Row) datum;
      if (sectionRow.row() == 0) {
        return row.params().isCategorySet() ?
          R.layout.hamburger_navigation_root_filter_view :
          R.layout.hamburger_navigation_top_filter_view;
      } else {
        return R.layout.hamburger_navigation_child_filter_view;
      }
    }
    return R.layout.hamburger_divider_view;
  }

  @NonNull
  @Override
  protected KSViewHolder viewHolder(@LayoutRes int layout, @NonNull View view) {
    switch (layout) {
      case R.layout.hamburger_navigation_header_logged_in_view:
        return new HamburgerNavigationHeaderLoggedInViewHolder(view);
      case R.layout.hamburger_navigation_header_logged_out_view:
        return new HamburgerNavigationHeaderLoggedOutViewHolder(view);
      case R.layout.hamburger_navigation_root_filter_view:
        return new HamburgerNavigationRootFilterViewHolder(view, delegate);
      case R.layout.hamburger_navigation_top_filter_view:
        return new HamburgerNavigationTopFilterViewHolder(view, delegate);
      case R.layout.hamburger_navigation_child_filter_view:
        return new HamburgerNavigationChildFilterViewHolder(view, delegate);
      default:
        return new EmptyViewHolder(view);
    }
  }

  public void initialize(final @NonNull Data data) {
    this.data().clear();
    this.data().add(Collections.singletonList(data.user()));
    for (final Data.Section section : data.sections()) {
      this.data().add(section.rows());
    }
//    this.data().add(Collections.singletonList(null));

    notifyDataSetChanged();

    // user
    // top filters
    // divider
    // category filters
  }

  @AutoParcel
  static public abstract class Data {
    public abstract @Nullable User user();
    public abstract List<Section> sections();

    @AutoParcel.Builder
    public abstract static class Builder {
      public abstract Builder user(User __);
      public abstract Builder sections(List<Section> __);
      public abstract Data build();
    }
    public static Builder builder() {
      return new AutoParcel_NavigationDrawerAdapter_Data.Builder()
        .user(null)
        .sections(ListUtils.empty());
    }
    public abstract Builder toBuilder();

    @AutoParcel
    static public abstract class Section {
      public abstract boolean expandable();
      public abstract boolean expanded();
      public abstract List<Row> rows();

      @AutoParcel.Builder
      public abstract static class Builder {
      public abstract Builder expandable(boolean __);
      public abstract Builder expanded(boolean __);
      public abstract Builder rows(List<Row> __);
      public abstract Section build();
    }
      public static Builder builder() {
        return new AutoParcel_NavigationDrawerAdapter_Data_Section.Builder()
          .expandable(false)
          .expanded(false)
          .rows(ListUtils.empty());
      }
      public abstract Builder toBuilder();

      @AutoParcel
      static public abstract class Row {
        public abstract @NonNull DiscoveryParams params();
        public abstract boolean selected();
        public abstract boolean root();

        @AutoParcel.Builder
        public static abstract class Builder {
          public abstract Builder params(DiscoveryParams __);
          public abstract Builder selected( boolean __);
          public abstract Builder root(boolean __);
          public abstract Row build();
        }
        public static Builder builder() {
          return new AutoParcel_NavigationDrawerAdapter_Data_Section_Row.Builder()
            .params(DiscoveryParams.builder().build())
            .selected(false)
            .root(false);
        }
        public abstract Builder toBuilder();
      }
    }





    public Data dataFromClickingRow(Section.Row clickedRow) {

      Builder builder = this.toBuilder();

      Section section = findSectionForRow(clickedRow);
      Section currentlyExpandedSection = findExpandedSection();




      return builder.build();
    }

    public Data expandSection(Section section) {

      ListUtils.replace(sections(), section, section.toBuilder().expanded(true).build());

      return null;
    }

    public Data collapseSection(Section section) {
      return null;
    }

    public Data selectRow(Section.Row row) {
      return null;
    }
    public Data deselectRow(Section.Row row) {
      return null;


//      ListUtils.replace(xs, oldx, newx)
    }

    public @Nullable Section findSectionForRow(Section.Row row) {
      for (Section section : sections()) {
        if (section.rows().contains(row)) {
          return section;
        }
      }
      return null;
    }

    public @Nullable Section findExpandedSection() {
      for (Section section : sections()) {
        if (section.expanded()) {
          return section;
        }
      }
      return null;
    }




  }
}
