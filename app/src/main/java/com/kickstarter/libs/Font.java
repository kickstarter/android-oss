package com.kickstarter.libs;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class Font {
  private Typeface ionIconsTypeface;
  private Typeface materialIconsTypeface;
  private Typeface tiemposTypeface;

  public Font(final AssetManager assetManager) {
    this.ionIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/ionicons.ttf");
    this.materialIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/MaterialIcons-Regular.ttf");
    this.tiemposTypeface = Typeface.createFromAsset(assetManager, "fonts/tiempos-regular.otf");
  }

  public Typeface ionIconsTypeface() {
    return ionIconsTypeface;
  }

  public Typeface materialIconsTypeface() {
    return materialIconsTypeface;
  }

  public Typeface tiemposTypeface() {
    return tiemposTypeface;
  }
}
