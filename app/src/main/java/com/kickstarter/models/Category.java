package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoParcel
@AutoGson
public abstract class Category implements Parcelable, Comparable<Category> {
  public abstract int color();
  public abstract long id();
  public abstract String name();
  public abstract @Nullable Category parent();
  public abstract @Nullable Long parentId();
  public abstract int position();
  public abstract @Nullable Integer projectsCount();
  public abstract String slug();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder color(int __);
    public abstract Builder id(long __);
    public abstract Builder name(String __);
    public abstract Builder parent(Category __);
    public abstract Builder parentId(Long __);
    public abstract Builder position(int __);
    public abstract Builder projectsCount(Integer __);
    public abstract Builder slug(String __);
    public abstract Category build();
  }

  public static Builder builder() {
    return new AutoParcel_Category.Builder();
  }

  public abstract Builder toBuilder();
  
  @Override
  public int compareTo(final @NonNull Category other) {
    if (id() == other.id()) {
      return 0;
    }

    if (isRoot() && id() == other.rootId()) {
      return -1;
    } else if (!isRoot() && rootId() == other.id()) {
      return 1;
    }

    return root().name().compareTo(other.root().name());
  }

  public boolean isRoot() {
    return parentId() == null || parentId() == 0;
  }

  public Category root() {
    return isRoot() ? this : parent();
  }

  public long rootId() {
    return isRoot() ? id() : parentId();
  }
}
