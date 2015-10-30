package com.kickstarter.models.pushdata;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class GCMPushData implements Parcelable {
  public abstract String alert();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder alert(String __);
    public abstract GCMPushData build();
  }

  public static Builder builder() {
    return new AutoParcel_GCMPushData.Builder();
  }

  public abstract Builder toBuilder();
}
