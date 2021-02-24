/*
 * Copyright 2014 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://github.com/JakeWharton/u2020/blob/b77f4e18751ee1e8fad8d7df25be86924d7d4a80/src/main/java/com/jakewharton/u2020/ui/misc/EnumAdapter.java
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */

package com.kickstarter.libs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    return this.enumConstants.length + this.nullOffset;
  }

  @Override
  public final T getItem(final int position) {
    if (this.showNull && position == 0) {
      return null;
    }

    return this.enumConstants[position - this.nullOffset];
  }

  @Override
  public final long getItemId(final int position) {
    return position;
  }

  @Override
  public View newView(final @NonNull LayoutInflater inflater, final int position, final @Nullable ViewGroup container) {
    return inflater.inflate(this.spinnerItemResource, container, false);
  }

  @Override
  public final void bindView(final @NonNull T item, final int position, final @NonNull View view) {
    final TextView tv =view.findViewById(android.R.id.text1);
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
