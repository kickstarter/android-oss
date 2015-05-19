package com.kickstarter.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class Avatar implements Parcelable {
  String thumb = null;
  String small = null;
  String medium = null;

  public String thumb() {
    return thumb;
  }

  public String small() {
    return small;
  }

  public String medium() {
    return medium;
  }
  @Override
  public int describeContents() { return 0; }

  @Override
  public void writeToParcel(Parcel dest, int flags) {AvatarParcelablePlease.writeToParcel(this, dest, flags);}
  public static final Creator<Avatar> CREATOR = new Creator<Avatar>() {
    public Avatar createFromParcel(Parcel source) {
      Avatar target = new Avatar();
      AvatarParcelablePlease.readFromParcel(target, source);
      return target;
    }
    public Avatar[] newArray(int size) {return new Avatar[size];}
  };
}
