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
  Category parent = null;
  Integer parentId = null;
  Integer projectsCount = null;

  public Integer color() {
    return KSColorUtils.setAlpha(color, 255);
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


  public Integer parentId() {
    return parentId;
  }

  public Integer projectsCount() {
    return projectsCount;
  }

  public Category root() {
    return isRoot() ? this : parent();
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
