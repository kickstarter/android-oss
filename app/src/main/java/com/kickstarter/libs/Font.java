package com.kickstarter.libs;

import android.content.res.AssetManager;
import android.graphics.Typeface;

public class Font {
  private Typeface ionIconsTypeface;
  private Typeface materialIconsTypeface;
  private Typeface ssKickstarterTypeface;
  private Typeface tiemposTypeface;

  public Font(final AssetManager assetManager) {
    this.ionIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/ionicons.ttf");
    this.materialIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/MaterialIcons-Regular.ttf");
    this.ssKickstarterTypeface = Typeface.createFromAsset(assetManager, "fonts/ss-kickstarter.otf");
    this.tiemposTypeface = Typeface.createFromAsset(assetManager, "fonts/tiempos-regular.otf");
  }

  public Typeface ionIconsTypeface() {
    return ionIconsTypeface;
  }

  public Typeface materialIconsTypeface() {
    return materialIconsTypeface;
  }

  public Typeface ssKickstarterTypeface() {
    return ssKickstarterTypeface;
  }

  public Typeface tiemposTypeface() {
    return tiemposTypeface;
  }
}
