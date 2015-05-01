package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class User implements Parcelable {
  Integer id = null;
  String name = null;

  static User currentUser;

  public Integer id() { return id; }
  public String name() { return name; }

  public static void setCurrent(final User user) {
    currentUser = user;
  }

  public static void unsetCurrent() {
    currentUser = null;
  }

  public static User current() {
    return currentUser;
  }

  public static boolean haveCurrent() {
    return currentUser != null;
  }

  public boolean isCurrent() {
    return this.id == User.current().id();
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
