package com.kickstarter.services.apirequests;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;

import androidx.annotation.Nullable;
import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class XauthBody implements Parcelable {
  public abstract String email();
  public abstract String password();
  public abstract @Nullable String code();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder email(String __);
    public abstract Builder password(String __);
    public abstract Builder code(String __);
    public abstract XauthBody build();
  }

  public static Builder builder() {
    return new AutoParcel_XauthBody.Builder();
  }

  public abstract Builder toBuilder();
}
