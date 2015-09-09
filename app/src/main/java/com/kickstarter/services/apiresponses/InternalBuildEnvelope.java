package com.kickstarter.services.apiresponses;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
public class InternalBuildEnvelope implements Parcelable {
  Integer build;
  String changelog;
  Boolean newerBuildAvailable;

  public Integer build() {
    return build;
  }

  public String changelog() {
    return changelog;
  }

  public Boolean newerBuildAvailable() {
    return newerBuildAvailable;
  }

  @Override
  public int describeContents() { return 0; }
  @Override
  public void writeToParcel(Parcel dest, int flags) {InternalBuildEnvelopeParcelablePlease.writeToParcel(this, dest, flags);}
  public static final Creator<InternalBuildEnvelope> CREATOR = new Creator<InternalBuildEnvelope>() {
    public InternalBuildEnvelope createFromParcel(Parcel source) {
      InternalBuildEnvelope target = new InternalBuildEnvelope();
      InternalBuildEnvelopeParcelablePlease.readFromParcel(target, source);
      return target;
    }
    public InternalBuildEnvelope[] newArray(int size) {return new InternalBuildEnvelope[size];}
  };
}
