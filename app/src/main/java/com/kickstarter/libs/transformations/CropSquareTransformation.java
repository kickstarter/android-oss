package com.kickstarter.libs.transformations;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.squareup.picasso.Transformation;

/**
 * From https://github.com/square/picasso/blob/000a72859ca82f28975d0ef5099b01ec110e73ea/website/index.html#L78-L91
 */
public class CropSquareTransformation implements Transformation {
  @Override public @NonNull Bitmap transform(@NonNull final Bitmap source) {
    final int size = Math.min(source.getWidth(), source.getHeight());
    final int x = (source.getWidth() - size) / 2;
    final int y = (source.getHeight() - size) / 2;
    final Bitmap result = Bitmap.createBitmap(source, x, y, size, size);
    if (result != source) {
      source.recycle();
    }
    return result;
  }

  @Override public String key() {
    return "square()";
  }
}
