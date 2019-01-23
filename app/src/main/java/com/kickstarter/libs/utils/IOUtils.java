package com.kickstarter.libs.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

public final class IOUtils {
  private IOUtils() {}

  public static byte[] readFully(final @NonNull InputStream inputStream) throws IOException {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final byte[] buffer = new byte[1024];
    for (int count; (count = inputStream.read(buffer)) != -1; ) {
      out.write(buffer, 0, count);
    }
    return out.toByteArray();
  }
}
