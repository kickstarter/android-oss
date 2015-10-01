package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class InternalBuildEnvelope implements Parcelable {
  @Nullable public abstract Integer build();
  @Nullable public abstract String changelog();
  public abstract boolean newerBuildAvailable();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder build(Integer __);
    public abstract Builder changelog(String __);
    public abstract Builder newerBuildAvailable(boolean __);
    public abstract InternalBuildEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_InternalBuildEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
