package com.kickstarter.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Font;

import javax.inject.Inject;

public class IconTextView extends TextView {
  @Inject Font font;

  // Enum for the iconType XML parameter
  private static final int MATERIAL = 0;
  private static final int SS_KICKSTARTER = 1;
  private static final int DEFAULT_ICON_TYPE = MATERIAL;

  private int iconType;

  public IconTextView(final @NonNull Context context) {
    super(context);
    initialize(context, null, 0, 0);
  }

  public IconTextView(final @NonNull Context context, final @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize(context, attrs, 0, 0);
  }

  public IconTextView(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context, attrs, defStyleAttr, 0);
  }

  @TargetApi(21)
  public IconTextView(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr,
    final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs, defStyleAttr, defStyleRes);
  }

  protected void initialize(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr,
    final int defStyleRes) {
    final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.IconTextView, defStyleAttr, defStyleRes);
    iconType = attributes.getInt(R.styleable.IconTextView_iconType, DEFAULT_ICON_TYPE);
    attributes.recycle();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);

    switch (iconType) {
      case MATERIAL:
        setTypeface(font.materialIconsTypeface());
        break;
      case SS_KICKSTARTER:
        setTypeface(font.ssKickstarterTypeface());
        break;
    }
  }
}
