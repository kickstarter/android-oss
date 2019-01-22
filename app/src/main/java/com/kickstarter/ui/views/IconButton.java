package com.kickstarter.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IconButton extends IconTextView {
  public IconButton(final @NonNull Context context) {
    super(context);
  }

  public IconButton(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public IconButton(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * For accessibility purposes, make this view behaves like a button. For example, if an {@link IconButton} has the
   * content description "Star", TalkBack will read out "Star Button". TalkBack also communicates that it is
   * a button in other languages using the appropriate grammar.
   */
  @Override
  public CharSequence getAccessibilityClassName() {
    return Button.class.getName();
  }
}
