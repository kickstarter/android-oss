package com.kickstarter.libs;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

public class Font {
  private Typeface materialIconsTypeface;
  private Typeface sansSerifLightTypeface;
  private Typeface sansSerifTypeface;
  private Typeface ssKickstarterTypeface;

  public Font(final @NonNull AssetManager assetManager) {
    this.materialIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/MaterialIcons-Regular.ttf");
    this.sansSerifLightTypeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
    this.sansSerifTypeface = Typeface.create("sans-serif", Typeface.NORMAL);
    try {
      this.ssKickstarterTypeface = Typeface.createFromAsset(assetManager, "fonts/ss-kickstarter.otf");
    } catch (RuntimeException e) {
      this.ssKickstarterTypeface = this.materialIconsTypeface;
    }
  }

  public Typeface materialIconsTypeface() {
    return materialIconsTypeface;
  }

  public Typeface sansSerifLightTypeface() {
    return sansSerifLightTypeface;
  }

  public Typeface sansSerifTypeface() {
    return sansSerifTypeface;
  }

  public Typeface ssKickstarterTypeface() {
    return ssKickstarterTypeface;
  }
}
