package com.kickstarter.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.KsrApplication;
import com.kickstarter.R;
import com.kickstarter.libs.Font;

import javax.inject.Inject;

public class IconTextView extends TextView {
  @Inject Font font;

  // Enum for the iconType XML parameter
  private static final int MATERIAL = 0;
  private static final int ION = 1;
  private static final int DEFAULT_ICON_TYPE = MATERIAL;

  private int iconType;

  public IconTextView(final Context context) {
    this(context, null);
  }

  public IconTextView(final Context context, final AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public IconTextView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public IconTextView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initialize(context, attrs, defStyleAttr, defStyleRes);
  }

  protected void initialize(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
    final TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.IconTextView, defStyleAttr, defStyleRes);
    iconType = attributes.getInt(R.styleable.IconTextView_iconType, DEFAULT_ICON_TYPE);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ((KsrApplication) getContext().getApplicationContext()).component().inject(this);

    switch (iconType) {
      case MATERIAL:
        setTypeface(font.materialIconsTypeface());
        break;
      case ION:
        setTypeface(font.ionIconsTypeface());
        break;
    }
  }
}
