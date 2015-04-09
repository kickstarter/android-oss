package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease public class Project implements Parcelable {
  Category category = null;
  Integer id = null;
  Location location = null;
  String name = null;
  Photo photo = null;

  public Category category() { return category; }
  public Integer id() { return id; }
  public Location location() { return location; }
  public String name() { return this.name; }
  public Photo photo() { return this.photo; }


  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    ProjectParcelablePlease.writeToParcel(this, dest, flags);
  }

  public static final Creator<Project> CREATOR = new Creator<Project>() {
    public Project createFromParcel(Parcel source) {
      Project target = new Project();
      ProjectParcelablePlease.readFromParcel(target, source);
      return target;
    }

    public Project[] newArray(int size) {
      return new Project[size];
    }
  };
}
