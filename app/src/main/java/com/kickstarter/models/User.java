package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class User implements Parcelable {
  public Avatar avatar = null;
  public Integer id = null;
  public String name = null;
  public String uid = null;

  public Avatar avatar() {
    return avatar;
  }

  public Integer id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String uid() {
    return uid;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    UserParcelablePlease.writeToParcel(this, dest, flags);
  }

  public static final Creator<User> CREATOR = new Creator<User>() {
    public User createFromParcel(Parcel source) {
      User target = new User();
      UserParcelablePlease.readFromParcel(target, source);
      return target;
    }

    public User[] newArray(int size) {
      return new User[size];
    }
  };
}
