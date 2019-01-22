package com.kickstarter.libs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    return "[start: " + this.start + ", length: " + this.length + "]";
  }

  @Override
  public boolean equals(final @Nullable Object o) {
    if (o instanceof Range) {
      final Range other = (Range) o;
      return this.start == other.start && this.length == other.length;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = this.start;
    result = 31 * result + this.length;
    return result;
  }
}
