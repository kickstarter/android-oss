package com.kickstarter.models.pushdata;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class Activity implements Parcelable {
  @com.kickstarter.models.Activity.Category public abstract String category();
  @Nullable public abstract Long commentId();
  public abstract long id();
  @Nullable public abstract Long projectId();
  @Nullable public abstract String projectPhoto();
  @Nullable public abstract Long updateId();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder commentId(Long __);
    public abstract Builder category(@com.kickstarter.models.Activity.Category String __);
    public abstract Builder id(long __);
    public abstract Builder projectId(Long __);
    public abstract Builder projectPhoto(String __);
    public abstract Builder updateId(Long __);
    public abstract Activity build();
  }

  public static Builder builder() {
    return new AutoParcel_Activity.Builder();
  }

  public abstract Builder toBuilder();

}
