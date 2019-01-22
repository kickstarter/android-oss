/**
 * Copyright 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://github.com/square/picasso/blob/000a72859ca82f28975d0ef5099b01ec110e73ea/website/index.html#L78-L91
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */
package com.kickstarter.libs.transformations;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

import androidx.annotation.NonNull;

public class CropSquareTransformation implements Transformation {
  @Override public @NonNull Bitmap transform(final @NonNull Bitmap source) {
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
