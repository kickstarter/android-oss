package com.kickstarter.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Font;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class IconTextView extends AppCompatTextView {
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

  protected void initialize(final @NonNull Context context, final @Nullable AttributeSet attrs, final int defStyleAttr,
    final int defStyleRes) {
    final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.IconTextView, defStyleAttr, defStyleRes);
    this.iconType = attributes.getInt(R.styleable.IconTextView_iconType, DEFAULT_ICON_TYPE);
    attributes.recycle();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ((KSApplication) getContext().getApplicationContext()).component().inject(this);

    switch (this.iconType) {
      case MATERIAL:
        setTypeface(this.font.materialIconsTypeface());
        break;
      case SS_KICKSTARTER:
        setTypeface(this.font.ssKickstarterTypeface());
        break;
    }
  }
}
