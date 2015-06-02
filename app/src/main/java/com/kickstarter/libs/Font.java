package com.kickstarter.libs;

import android.app.Application;
import android.graphics.Typeface;

public class Font {
  private Typeface ionIconsTypeface;
  private Typeface materialIconsTypeface;

  public Font(final Application application) {
    this.ionIconsTypeface = Typeface.createFromAsset(application.getAssets(), "fonts/ionicons.ttf");
    this.materialIconsTypeface = Typeface.createFromAsset(application.getAssets(), "fonts/MaterialIcons-Regular.ttf");
  }

  public Typeface ionIconsTypeface() {
    return ionIconsTypeface;
  }

  public Typeface materialIconsTypeface() {
    return materialIconsTypeface;
  }
}
