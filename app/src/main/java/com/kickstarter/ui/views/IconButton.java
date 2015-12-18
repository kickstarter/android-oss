package com.kickstarter.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Button;

public class IconButton extends IconTextView {
  public IconButton(@NonNull final Context context) {
    super(context);
  }

  public IconButton(@NonNull final Context context, @Nullable final AttributeSet attrs) {
    super(context, attrs);
  }

  public IconButton(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
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
