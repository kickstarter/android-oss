package com.kickstarter.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.kickstarter.R;

public class ProjectCardView extends android.support.v7.widget.CardView {
  public ProjectCardView(Context context) {
    this(context, null, 0);
  }

  public ProjectCardView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ProjectCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    // TODO: Could probably extract the icons into a separate textview class.
    // Should also create a font manager that loads the fonts once - this method
    // is wasteful
    Typeface typeface = Typeface.createFromAsset(
      getContext().getAssets(),
      "fonts/ionicons.ttf");
    TextView category_icon = (TextView) findViewById(R.id.category_icon);
    category_icon.setTypeface(typeface);
    TextView location_icon = (TextView) findViewById(R.id.location_icon);
    location_icon.setTypeface(typeface);
  }
}
