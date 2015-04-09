package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease public class Location implements Parcelable {
  Integer id = null;
  String name = null;

  public Integer id() { return id; }
  public String name() { return name; }


  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    LocationParcelablePlease.writeToParcel(this, dest, flags);
  }

  public static final Creator<Location> CREATOR = new Creator<Location>() {
    public Location createFromParcel(Parcel source) {
      Location target = new Location();
      LocationParcelablePlease.readFromParcel(target, source);
      return target;
    }

    public Location[] newArray(int size) {
      return new Location[size];
    }
  };
}
