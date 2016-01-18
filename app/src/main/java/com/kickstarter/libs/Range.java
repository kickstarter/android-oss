package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class Range {
  public final int start;
  public final int length;

  public Range(final int start, final int length) {
    this.start = start;
    this.length = length;
  }

  public static @NonNull Range create(final int start, final int length) {
    return new Range(start, length);
  }

  @Override
  public String toString() {
    return "[start: " + start + ", length: " + length + "]";
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (o instanceof Range) {
      final Range other = (Range) o;
      return start == other.start && length == other.length;
    }
    return false;
  }
}
