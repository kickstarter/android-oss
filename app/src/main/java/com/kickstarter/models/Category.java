package com.kickstarter.models;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kickstarter.R;
import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.libs.KSColorUtils;

import auto.parcel.AutoParcel;

@AutoParcel
@AutoGson
abstract public class Category implements Parcelable {
  public abstract int color();
  public abstract long id();
  public abstract String name();
  @Nullable public abstract Category parent();
  @Nullable public abstract Long parentId();
  public abstract int position();
  @Nullable public abstract Integer projectsCount();
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

  public @ColorInt int colorWithAlpha() {
    return KSColorUtils.setAlpha(color(), 255);
  }

  public int discoveryFilterCompareTo(@NonNull final Category other) {
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

  public int overlayTextColor(@NonNull final Context context) {
    final Resources resources = context.getResources();
    return KSColorUtils.isLight(colorWithAlpha()) ?
      resources.getColor(R.color.text_dark) :
      resources.getColor(R.color.white);
  }

  public Category root() {
    return isRoot() ? this : parent();
  }

  public long rootId() {
    return isRoot() ? id() : parentId();
  }
}
