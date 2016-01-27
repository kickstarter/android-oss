package com.kickstarter.libs.utils;

import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtils {
  public static byte[] readFully(final @NonNull InputStream inputStream) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    for (int count; (count = inputStream.read(buffer)) != -1; ) {
      out.write(buffer, 0, count);
    }
    return out.toByteArray();
  }
}
