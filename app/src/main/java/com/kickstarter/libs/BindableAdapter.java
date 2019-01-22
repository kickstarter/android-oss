/*
 * Copyright 2013 Jake Wharton
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
 * Original: https://github.com/JakeWharton/u2020/blob/b77f4e18751ee1e8fad8d7df25be86924d7d4a80/src/main/java/com/jakewharton/u2020/ui/misc/BindableAdapter.java
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */

package com.kickstarter.libs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** An implementation of {@link BaseAdapter} which uses the new/bind pattern for its views. */
public abstract class BindableAdapter<T> extends BaseAdapter {
  private final Context context;
  private final LayoutInflater inflater;

  public BindableAdapter(final @NonNull Context context) {
    this.context = context;
    this.inflater = LayoutInflater.from(context);
  }

  public Context getContext() {
    return this.context;
  }

  @Override public abstract T getItem(final int position);

  @Override public final View getView(final int position, final @Nullable View initialView, final ViewGroup container) {
    View view = initialView;
    if (view == null) {
      view = newView(this.inflater, position, container);
      if (view == null) {
        throw new IllegalStateException("newView result must not be null.");
      }
    }
    bindView(getItem(position), position, view);
    return view;
  }

  /** Create a new instance of a view for the specified position. */
  public abstract View newView(final @NonNull LayoutInflater inflater, final int position, final @Nullable ViewGroup container);

  /** Bind the data for the specified {@code position} to the view. */
  public abstract void bindView(final T item, final int position, final View view);

  @Override public final View getDropDownView(final int position, final View initialView, final @Nullable ViewGroup container) {
    View view = initialView;
    if (view == null) {
      view = newDropDownView(this.inflater, position, container);
      if (view == null) {
        throw new IllegalStateException("newDropDownView result must not be null.");
      }
    }
    bindDropDownView(getItem(position), position, view);
    return view;
  }

  /** Create a new instance of a drop-down view for the specified position. */
  public View newDropDownView(final @NonNull LayoutInflater inflater, final int position, final @Nullable ViewGroup container) {
    return newView(inflater, position, container);
  }

  /** Bind the data for the specified {@code position} to the drop-down view. */
  public void bindDropDownView(final @NonNull T item, final int position, final @NonNull View view) {
    bindView(item, position, view);
  }
}
