package com.kickstarter;

import android.app.Application;
import android.graphics.Typeface;

public class Font {
  private Typeface ionIconTypeface;

  public Font(Application application) {
    this.ionIconTypeface = Typeface.createFromAsset(application.getAssets(), "fonts/ionicons.ttf");
  }

  public Typeface getIonIconTypeface() {
    return ionIconTypeface;
  }
}
