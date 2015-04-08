package com.kickstarter;

import android.app.Application;
import android.graphics.Typeface;

public class FontManager {
  private final Application application;
  private Typeface ionIconTypeface;

  public FontManager(Application application) {
    this.application = application;
    this.ionIconTypeface = Typeface.createFromAsset(application.getAssets(), "fonts/ionicons.ttf");
  }

  public Typeface getIonIconTypeface() {
    return ionIconTypeface;
  }
}
