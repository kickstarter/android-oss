package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kickstarter.ui.viewholders.KSViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class KSAdapter extends RecyclerView.Adapter<KSViewHolder> {
  private List<List<Object>> sections = new ArrayList<>();

  public List<List<Object>> sections() {
    return sections;
  }

  public void clearSections() {
    sections.clear();
  }

  public <T> void addSection(final @NonNull List<T> section) {
    sections.add(new ArrayList<>(section));
  }

  public <T> void addSections(final @NonNull List<List<T>> sections) {
    for (final List<T> section : sections) {
      addSection(section);
    }
  }

  public <T> void setSection(final int location, final @NonNull List<T> section) {
    sections.set(location, new ArrayList<>(section));
  }

  public <T> void insertSection(final int location, final @NonNull List<T> section) {
    sections.add(location, new ArrayList<>(section));
  }


  /**
   * Fetch the layout id associated with a sectionRow.
   */
  protected abstract int layout(final @NonNull SectionRow sectionRow);

  /**
   * Returns a new KSViewHolder given a layout and view.
   */
  protected abstract @NonNull KSViewHolder viewHolder(@LayoutRes final int layout, final @NonNull View view);

  @Override
  public final @NonNull KSViewHolder onCreateViewHolder(final @NonNull ViewGroup viewGroup, @LayoutRes final int layout) {
    final View view = inflateView(viewGroup, layout);
    return viewHolder(layout, view);
  }

  @Override
  public final void onBindViewHolder(final @NonNull KSViewHolder viewHolder, final int position) {
    viewHolder.onBind(objectFromPosition(position));
  }

  @Override
  public final int getItemViewType(final int position) {
    return layout(sectionRowFromPosition(position));
  }

  @Override
  public final int getItemCount() {
    int itemCount = 0;
    for (final List<?> section : sections) {
      itemCount += section.size();
    }

    return itemCount;
  }

  /**
   * Gets the data object associated with a sectionRow.
   */
  protected Object objectFromSectionRow(final @NonNull SectionRow sectionRow) {
    return sections.get(sectionRow.section()).get(sectionRow.row());
  }

  protected int sectionCount(final int section) {
    if (section > sections().size() - 1) {
      return 0;
    }
    return sections().get(section).size();
  }

  /**
   * Gets the data object associated with a position.
   */
  private Object objectFromPosition(final int position) {
    return objectFromSectionRow(sectionRowFromPosition(position));
  }

  private @NonNull SectionRow sectionRowFromPosition(final int position) {
    final SectionRow sectionRow = new SectionRow();
    int cursor = 0;
    for (final List<?> section : sections) {
      for (final Object __ : section) {
        if (cursor == position) {
          return sectionRow;
        }
        cursor++;
        sectionRow.nextRow();
      }
      sectionRow.nextSection();
    }

    throw new RuntimeException("Position " + position + " not found in sections");
  }

  private @NonNull View inflateView(final @NonNull ViewGroup viewGroup, @LayoutRes final int viewType) {
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
