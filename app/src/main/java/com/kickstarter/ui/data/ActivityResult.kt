package com.kickstarter.ui.data;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import auto.parcel.AutoParcel;

@AutoParcel
public abstract class ActivityResult implements Parcelable {
  public abstract int requestCode();
  public abstract int resultCode();
  public abstract @Nullable Intent intent();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder requestCode(int __);
    public abstract Builder resultCode(int __);
    public abstract Builder intent(Intent __);
    public abstract ActivityResult build();
  }

  public static @NonNull ActivityResult create(final int requestCode, final int resultCode, final @Nullable Intent intent) {
    return ActivityResult.builder()
      .requestCode(requestCode)
      .resultCode(resultCode)
      .intent(intent)
      .build();
  }

  public static Builder builder() {
    return new AutoParcel_ActivityResult.Builder();
  }

  public abstract Builder toBuilder();

  public boolean isCanceled() {
    return resultCode() == Activity.RESULT_CANCELED;
  }

  public boolean isOk() {
    return resultCode() == Activity.RESULT_OK;
  }

  public boolean isRequestCode(final int v) {
    return requestCode() == v;
  }
}
