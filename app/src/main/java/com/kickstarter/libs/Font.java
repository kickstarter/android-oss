package com.kickstarter.libs;

import android.app.Application;
import android.graphics.Typeface;

public class Font {
  private Typeface ionIconTypeface;

  public Font(final Application application) {
    this.ionIconTypeface = Typeface.createFromAsset(application.getAssets(), "fonts/ionicons.ttf");
  }

  public Typeface ionIconTypeface() {
    return ionIconTypeface;
  }
}
