package com.kickstarter.libs;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
  @LayoutRes private final int spinnerItemResource;
  private final int nullOffset;

  public EnumAdapter(final @NonNull Context context, final @NonNull Class<T> enumType, final boolean showNull, final @LayoutRes int spinnerItemResource) {
    super(context);
    this.enumConstants = enumType.getEnumConstants();
    this.showNull = showNull;
    this.spinnerItemResource = spinnerItemResource;
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
  public View newView(final @NonNull LayoutInflater inflater, final int position, final @Nullable ViewGroup container) {
    return inflater.inflate(spinnerItemResource, container, false);
  }

  @Override
  public final void bindView(final @NonNull T item, final int position, final @NonNull View view) {
    final TextView tv = ButterKnife.findById(view, android.R.id.text1);
    tv.setText(getName(item));
  }

  @Override
  public final View newDropDownView(final @NonNull LayoutInflater inflater, final int position, final @Nullable ViewGroup container) {
    return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false);
  }

  protected String getName(final @NonNull T item) {
    return String.valueOf(item);
  }
}
