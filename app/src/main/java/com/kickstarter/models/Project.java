package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Project implements Parcelable {
  Category category = null;
  Float goal = null;
  Integer id = null;
  Location location = null;
  String name = null;
  Float pledged = null;
  Photo photo = null;

  public Category category() { return category; }
  public Float goal() { return goal; }
  public Integer id() { return id; }
  public Location location() { return location; }
  public String name() { return name; }
  public Float pledged() { return pledged; }
  public Photo photo() { return photo; }

  public float percentageFunded() {
    if (goal > 0.0f)
      return (pledged / goal) * 100.0f;

    return 0.0f;
  }

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
