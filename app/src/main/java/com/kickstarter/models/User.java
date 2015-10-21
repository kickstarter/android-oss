package com.kickstarter.models;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class User implements Parcelable {
  public abstract Avatar avatar();
  public abstract long id();
  public abstract String name();
  @Nullable public abstract String uid();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder avatar(Avatar __);
    public abstract Builder id(long __);
    public abstract Builder name(String __);
    public abstract Builder uid(String __);
    public abstract User build();
  }

  public static Builder builder() {
    return new AutoParcel_User.Builder();
  }

  public String param() {
    return String.valueOf(this.id());
  }

  public abstract Builder toBuilder();
}
