package com.kickstarter.models;

import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.jetbrains.annotations.Nullable;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Avatar implements Parcelable {
  public abstract @Nullable String medium();
  public abstract @Nullable String small();
  public abstract @Nullable String thumb();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder medium(String __);
    public abstract Builder small(String __);
    public abstract Builder thumb(String __);
    public abstract Avatar build();
  }

  public static Builder builder() {
    return new AutoParcel_Avatar.Builder();
  }
}
