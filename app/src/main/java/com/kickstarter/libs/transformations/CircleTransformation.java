package com.kickstarter.libs.transformations;

/*
 * Copyright 2014 Julian Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ***
 *
 * Original: https://gist.github.com/julianshen/5829333
 * Modifications: Some modifiers and annotations have been added by Kickstarter.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

import androidx.annotation.NonNull;

public class CircleTransformation implements Transformation {
  private int x;
  private int y;

  @Override
  public Bitmap transform(final @NonNull Bitmap source) {
    final int size = Math.min(source.getWidth(), source.getHeight());

    this.x = (source.getWidth() - size) / 2;
    this.y = (source.getHeight() - size) / 2;

    final Bitmap squaredBitmap = Bitmap.createBitmap(source, this.x, this.y, size, size);
    if (squaredBitmap != source) {
      source.recycle();
    }

    final Bitmap.Config config = source.getConfig() != null ? source.getConfig() : Bitmap.Config.ARGB_8888;
    final Bitmap bitmap = Bitmap.createBitmap(size, size, config);

    final Canvas canvas = new Canvas(bitmap);
    final Paint paint = new Paint();
    final BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
    paint.setShader(shader);
    paint.setAntiAlias(true);

    final float r = size/2f;
    canvas.drawCircle(r, r, r, paint);

    squaredBitmap.recycle();
    return bitmap;
  }

  @Override
  public String key() {
    return "circle(x=" + this.x + ",y=" + this.y + ")";
  }
}
