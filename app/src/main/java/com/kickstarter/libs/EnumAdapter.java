package com.kickstarter.libs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;

// Source from:
// https://github.com/JakeWharton/u2020/blob/b77f4e18751ee1e8fad8d7df25be86924d7d4a80/src/main/java/com/jakewharton/u2020/ui/misc/EnumAdapter.java

public class EnumAdapter<T extends Enum<T>> extends BindableAdapter<T> {
  private final T[] enumConstants;
  private final boolean showNull;
  private final int nullOffset;

  public EnumAdapter(final Context context, final Class<T> enumType) {
    this(context, enumType, false);
  }

  public EnumAdapter(final Context context, final Class<T> enumType, final boolean showNull) {
    super(context);
    this.enumConstants = enumType.getEnumConstants();
    this.showNull = showNull;
    this.nullOffset = showNull ? 1 : 0;
  }

  @Override
  public final int getCount() {
    return enumConstants.length + nullOffset;
  }

  @Override
  public final T getItem(final int position) {
    if (showNull && position == 0) {
      return null;
    }

    return enumConstants[position - nullOffset];
  }

  @Override
  public final long getItemId(final int position) {
    return position;
  }

  @Override
  public View newView(final LayoutInflater inflater, final int position, final ViewGroup container) {
    return inflater.inflate(android.R.layout.simple_spinner_item, container, false);
  }

  @Override
  public final void bindView(final T item, final int position, final View view) {
    TextView tv = ButterKnife.findById(view, android.R.id.text1);
    tv.setText(getName(item));
  }

  @Override
  public final View newDropDownView(final LayoutInflater inflater, final int position, final ViewGroup container) {
    return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false);
  }

  protected String getName(final T item) {
    return String.valueOf(item);
  }
}
