package com.kickstarter.models;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import org.joda.time.DateTime;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Message implements Parcelable {
  public abstract String body();
  public abstract DateTime createdAt();
  public abstract long id();
  public abstract User recipient();
  public abstract User sender();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder body(String __);
    public abstract Builder createdAt(DateTime __);
    public abstract Builder id(long __);
    public abstract Builder recipient(User __);
    public abstract Builder sender(User __);
    public abstract Message build();
  }

  public static Builder builder() {
    return new AutoParcel_Message.Builder();
  }

  public abstract Builder toBuilder();
}
