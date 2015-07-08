package com.kickstarter.libs;

// Source from:
// https://github.com/JakeWharton/u2020/blob/b77f4e18751ee1e8fad8d7df25be86924d7d4a80/src/main/java/com/jakewharton/u2020/ui/misc/BindableAdapter.java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/** An implementation of {@link BaseAdapter} which uses the new/bind pattern for its views. */
public abstract class BindableAdapter<T> extends BaseAdapter {
  private final Context context;
  private final LayoutInflater inflater;

  public BindableAdapter(Context context) {
    this.context = context;
    this.inflater = LayoutInflater.from(context);
  }

  public Context getContext() {
    return context;
  }

  @Override public abstract T getItem(int position);

  @Override public final View getView(int position, View view, ViewGroup container) {
    if (view == null) {
      view = newView(inflater, position, container);
      if (view == null) {
        throw new IllegalStateException("newView result must not be null.");
      }
    }
    bindView(getItem(position), position, view);
    return view;
  }

  /** Create a new instance of a view for the specified position. */
  public abstract View newView(LayoutInflater inflater, int position, ViewGroup container);

  /** Bind the data for the specified {@code position} to the view. */
  public abstract void bindView(T item, int position, View view);

  @Override public final View getDropDownView(int position, View view, ViewGroup container) {
    if (view == null) {
      view = newDropDownView(inflater, position, container);
      if (view == null) {
        throw new IllegalStateException("newDropDownView result must not be null.");
      }
    }
    bindDropDownView(getItem(position), position, view);
    return view;
  }

  /** Create a new instance of a drop-down view for the specified position. */
  public View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
    return newView(inflater, position, container);
  }

  /** Bind the data for the specified {@code position} to the drop-down view. */
  public void bindDropDownView(T item, int position, View view) {
    bindView(item, position, view);
  }
}
