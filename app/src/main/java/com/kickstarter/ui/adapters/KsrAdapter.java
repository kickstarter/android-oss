package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.ui.viewholders.KsrViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class KsrAdapter extends RecyclerView.Adapter<KsrViewHolder> {
  private List<List<?>> data = new ArrayList<>();

  public List<List<?>> data() {
    return data;
  }

  /**
   * Fetch the layout id associated with a sectionRow.
   */
  protected abstract int layout(@NonNull final SectionRow sectionRow);

  /**
   * Returns a new KsrViewHolder given a layout and view.
   */
  protected abstract KsrViewHolder viewHolder(final int layout, @NonNull final View view);

  @Override
  public final KsrViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, @LayoutRes final int layout) {
    final View view = inflateView(viewGroup, layout);
    return viewHolder(layout, view);
  }

  @Override
  public final void onBindViewHolder(@NonNull final KsrViewHolder viewHolder, final int position) {
    viewHolder.onBind(objectFromPosition(position));
  }

  @Override
  public final int getItemViewType(final int position) {
    return layout(sectionRowFromPosition(position));
  }

  @Override
  public final int getItemCount() {
    int itemCount = 0;
    for (final List<?> section : data) {
      itemCount += section.size();
    }

    return itemCount;
  }

  /**
   * Gets the data object associated with a sectionRow.
   */
  protected Object objectFromSectionRow(@NonNull final SectionRow sectionRow) {
    return data.get(sectionRow.section()).get(sectionRow.row());
  }

  /**
   * Gets the data object associated with a position.
   */
  private Object objectFromPosition(final int position) {
    return objectFromSectionRow(sectionRowFromPosition(position));
  }

  private SectionRow sectionRowFromPosition(final int position) {
    final SectionRow sectionRow = new SectionRow();
    int cursor = 0;
    for (final List<?> section : data) {
      for (final Object __ : section) {
        if (cursor == position) {
          return sectionRow;
        }
        cursor++;
        sectionRow.nextRow();
      }
      sectionRow.nextSection();
    }

    throw new RuntimeException("Position " + position + " not found in data");
  }

  private View inflateView(@NonNull final ViewGroup viewGroup, @LayoutRes final int viewType) {
    final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
    return layoutInflater.inflate(viewType, viewGroup, false);
  }

  /**
   * SectionRows allow RecyclerViews to be structured into one or more sections. Sections can contain one or more rows.
   */
  protected class SectionRow {
    private int section;
    private int row;

    public SectionRow() {
      section = 0;
      row = 0;
    }

    public SectionRow(final int section, final int row) {
      this.section = section;
      this.row = row;
    }

    public int section() {
      return section;
    }

    public int row() {
      return row;
    }

    protected void nextRow() {
      row++;
    }

    protected void nextSection() {
      section++;
      row = 0;
    }
  }
}
