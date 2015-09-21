package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Category implements Parcelable {
  Integer id = null;
  String name = null;
  Integer parentId = null;
  Integer position = null;
  Category parent = null;

  public Integer id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Integer parentId() {
    return parentId;
  }

  public Integer position() {
    return position;
  }

  // TODO: Should be able to grab a Category rather than dealing with Integers. That would require loading all the
  // categories (probably on start-up). There is a bunch of work to do on categories, probably best to tackle it all
  // at once.
  public Integer rootId() {
    return isRoot() ? id() : parentId();
  }

  public Category parent() {
    return parent;
  }

  public boolean isRoot() {
    return parentId() == null || parentId() == 0;
  }

  public int discoveryFilterCompareTo(final Category category) {
    if (isRoot()) {
      return name.compareTo(category.root().name());
    } else if (category.isRoot()) {
      return root().name().compareTo(category.name());
    }

    return name.compareTo(category.name());
  }

  public Category root() {
    return parent == null ? this : parent;
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
}
