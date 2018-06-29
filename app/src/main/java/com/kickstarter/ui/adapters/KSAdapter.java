package com.kickstarter.ui.adapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.kickstarter.BuildConfig;
import com.kickstarter.libs.utils.ExceptionUtils;
import com.kickstarter.ui.viewholders.KSViewHolder;
import com.trello.rxlifecycle.ActivityEvent;


import java.util.ArrayList;
import java.util.List;

public abstract class KSAdapter extends RecyclerView.Adapter<KSViewHolder> {
  private List<List<Object>> sections = new ArrayList<>();

  public List<List<Object>> sections() {
    return this.sections;
  }

  public void clearSections() {
    this.sections.clear();
  }

  public <T> void addSection(final @NonNull List<T> section) {
    this.sections.add(new ArrayList<>(section));
  }

  public <T> void addSections(final @NonNull List<List<T>> sections) {
    for (final List<T> section : sections) {
      addSection(section);
    }
  }

  public <T> void setSection(final int location, final @NonNull List<T> section) {
    this.sections.set(location, new ArrayList<>(section));
  }

  public <T> void insertSection(final int location, final @NonNull List<T> section) {
    this.sections.add(location, new ArrayList<>(section));
  }

  /**
   * Fetch the layout id associated with a sectionRow.
   */
  protected abstract int layout(final @NonNull SectionRow sectionRow);

  /**
   * Returns a new KSViewHolder given a layout and view.
   */
  protected abstract @NonNull KSViewHolder viewHolder(final @LayoutRes int layout, final @NonNull View view);

  @Override
  public void onViewDetachedFromWindow(final @NonNull KSViewHolder holder) {
    super.onViewDetachedFromWindow(holder);

    // View holders are "stopped" when they are detached from the window for recycling
    holder.lifecycleEvent(ActivityEvent.STOP);

    // View holders are "destroy" when they are detached from the window and no adapter is listening
    // to events, so ostensibly the view holder is being deallocated.
    if (!hasObservers()) {
      holder.lifecycleEvent(ActivityEvent.DESTROY);
    }
  }

  @Override
  public void onViewAttachedToWindow(final @NonNull KSViewHolder holder) {
    super.onViewAttachedToWindow(holder);

    // View holders are "started" when they are attached to the new window because this means
    // it has been recycled.
    holder.lifecycleEvent(ActivityEvent.START);
  }

  @Override
  public final @NonNull KSViewHolder onCreateViewHolder(final @NonNull ViewGroup viewGroup, final @LayoutRes int layout) {
    final View view = inflateView(viewGroup, layout);
    final KSViewHolder viewHolder = viewHolder(layout, view);

    viewHolder.lifecycleEvent(ActivityEvent.CREATE);

    return viewHolder;
  }

  @Override
  public final void onBindViewHolder(final @NonNull KSViewHolder viewHolder, final int position) {
    final Object data = objectFromPosition(position);

    try {
      viewHolder.bindData(data);
      viewHolder.onBind();
    } catch (final Exception e) {
      if (BuildConfig.DEBUG) {
        ExceptionUtils.rethrowAsRuntimeException(e);
      } else {
        // TODO: alter the exception message to say we are just reporting it and it's not a real crash.
        Crashlytics.logException(e);
      }
    }
  }

  @Override
  public final int getItemViewType(final int position) {
    return layout(sectionRowFromPosition(position));
  }

  @Override
  public final int getItemCount() {
    int itemCount = 0;
    for (final List<?> section : this.sections) {
      itemCount += section.size();
    }

    return itemCount;
  }

  /**
   * Gets the data object associated with a sectionRow.
   */
  protected Object objectFromSectionRow(final @NonNull SectionRow sectionRow) {
    return this.sections.get(sectionRow.section()).get(sectionRow.row());
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
  protected Object objectFromPosition(final int position) {
    return objectFromSectionRow(sectionRowFromPosition(position));
  }

  private @NonNull SectionRow sectionRowFromPosition(final int position) {
    final SectionRow sectionRow = new SectionRow();
    int cursor = 0;
    for (final List<?> section : this.sections) {
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

  private @NonNull View inflateView(final @NonNull ViewGroup viewGroup, final @LayoutRes int viewType) {
    final LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
    return layoutInflater.inflate(viewType, viewGroup, false);
  }

  /**
   * SectionRows allow RecyclerViews to be structured into sections of rows.
   */
  protected class SectionRow {
    private int section;
    private int row;

    public SectionRow() {
      this.section = 0;
      this.row = 0;
    }

    public SectionRow(final int section, final int row) {
      this.section = section;
      this.row = row;
    }

    public int section() {
      return this.section;
    }

    public int row() {
      return this.row;
    }

    protected void nextRow() {
      this.row++;
    }

    protected void nextSection() {
      this.section++;
      this.row = 0;
    }
  }
}
