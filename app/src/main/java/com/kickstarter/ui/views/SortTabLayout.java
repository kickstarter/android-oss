package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;

public final class SortTabLayout extends TabLayout {

  public SortTabLayout(final @NonNull Context context) {
    super(context);
  }

  public SortTabLayout(final @NonNull Context context, final @NonNull AttributeSet attrs) {
    super(context, attrs);
  }

  public SortTabLayout(final @NonNull Context context, final @NonNull AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
