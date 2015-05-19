package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Category implements Parcelable {
  Integer id = null;
  String name = null;

  public Integer id() { return id; }
  public String name() { return this.name; }

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
