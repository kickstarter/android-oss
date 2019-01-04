package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson @AutoParcel
public abstract class InternalBuildEnvelope implements Parcelable {
  public abstract @Nullable Integer build();
  public abstract @Nullable String changelog();
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
