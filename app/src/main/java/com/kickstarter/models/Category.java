package com.kickstarter.models;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.R;
import com.kickstarter.libs.KSColorUtils;

@ParcelablePlease
public class Category implements Parcelable {
  public Integer color = null;
  public Integer id = null;
  public String name = null;
  public Category parent = null;
  public Integer parentId = null;
  public Integer position = null;
  public Integer projectsCount = null;
  public String slug = null;

  public Integer color() {
    return KSColorUtils.setAlpha(color, 255);
  }

  public int discoveryFilterCompareTo(@NonNull final Category other) {
    if (id.equals(other.id())) {
      return 0;
    }

    if (isRoot() && id.equals(other.rootId())) {
      return -1;
    } else if (!isRoot() && rootId().equals(other.id())) {
      return 1;
    }

    return root().name().compareTo(other.root().name());
  }

  public Integer id() {
    return id;
  }

  public boolean isRoot() {
    return parentId() == null || parentId() == 0;
  }

  public String name() {
    return name;
  }

  public int overlayTextColor(final Context context) {
    final Resources resources = context.getResources();
    return KSColorUtils.isLight(color()) ? resources.getColor(R.color.text_dark) : resources.getColor(R.color.white);
  }

  public Category parent() {
    if (parent == null) {
      parent = new Category();
      parent.id = parentId;
    }

    return parent;
  }

  public void parent(@NonNull final Category parent) {
    this.parent = parent;
    parentId = parent.id();
  }

  public Integer parentId() {
    return parentId;
  }

  public Integer position() {
    return position;
  }
  public Integer projectsCount() {
    return projectsCount;
  }

  public Category root() {
    return isRoot() ? this : parent();
  }

  public Integer rootId() {
    return isRoot() ? id() : parentId();
  }

  public String slug() {
    return slug;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    CategoryParcelablePlease.writeToParcel(this, dest, flags);
  }

  public static final Creator<Category> CREATOR = new Creator<Category>() {
    public Category createFromParcel(Parcel source) {
      Category target = new Category();
      CategoryParcelablePlease.readFromParcel(target, source);
      return target;
    }

    public Category[] newArray(int size) {
      return new Category[size];
    }
  };

  public boolean equals(final Category category) {
    if (id == null || category.id == null) {
      return false;
    }
    return true;
  }
}
