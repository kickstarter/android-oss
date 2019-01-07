package com.kickstarter.libs;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import androidx.annotation.NonNull;

public class Font {
  private Typeface maisonNeueBookTypeface;
  private Typeface materialIconsTypeface;
  private Typeface sansSerifLightTypeface;
  private Typeface sansSerifTypeface;
  private Typeface ssKickstarterTypeface;

  public Font(final @NonNull AssetManager assetManager) {
    this.maisonNeueBookTypeface = Typeface.createFromAsset(assetManager, "fonts/maison-neue-book.ttf");
    this.materialIconsTypeface = Typeface.createFromAsset(assetManager, "fonts/MaterialIcons-Regular.ttf");
    this.sansSerifLightTypeface = Typeface.create("sans-serif-light", Typeface.NORMAL);
    this.sansSerifTypeface = Typeface.create("sans-serif", Typeface.NORMAL);
    try {
      this.ssKickstarterTypeface = Typeface.createFromAsset(assetManager, "fonts/ss-kickstarter.otf");
    } catch (RuntimeException e) {
      this.ssKickstarterTypeface = this.materialIconsTypeface;
    }
  }

  public Typeface maisonNeueBookTypeface() {
    return this.maisonNeueBookTypeface;
  }

  public Typeface materialIconsTypeface() {
    return this.materialIconsTypeface;
  }

  public Typeface sansSerifLightTypeface() {
    return this.sansSerifLightTypeface;
  }

  public Typeface sansSerifTypeface() {
    return this.sansSerifTypeface;
  }

  public Typeface ssKickstarterTypeface() {
    return this.ssKickstarterTypeface;
  }
}
