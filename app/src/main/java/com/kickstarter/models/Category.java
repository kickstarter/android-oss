package com.kickstarter.models;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.kickstarter.R;
import com.kickstarter.libs.KSColorUtils;

@ParcelablePlease
public class Category implements Parcelable {
  Integer color = null;
  Integer id = null;
  String name = null;
  Integer parentId = null;
  Category parent = null;
  Integer projectsCount = null;

  public Integer color() {
    return KSColorUtils.setAlpha(color, 255);
  }

  public Integer id() {
    return id;
  }

  public String name() {
    return name;
  }

  public Integer parentId() {
    return parentId;
  }

  public Category parent() {
    if (parent == null) {
      parent = new Category();
      parent.id = parentId;
    }

    return parent;
  }

  public Integer projectsCount() {
    return projectsCount;
  }

  // TODO: Should be able to grab a Category rather than dealing with Integers. That would require loading all the
  // categories (probably on start-up). There is a bunch of work to do on categories, probably best to tackle it all
  // at once.
  public Integer rootId() {
    return isRoot() ? id() : parentId();
  }

  public boolean isRoot() {
    return parentId() == null || parentId() == 0;
  }

  public Category root() {
    return isRoot() ? this : parent();
  }

  public int overlayTextColor(final Context context) {
    final Resources resources = context.getResources();
    return KSColorUtils.isLight(color()) ? resources.getColor(R.color.text_dark) : resources.getColor(R.color.white);
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
