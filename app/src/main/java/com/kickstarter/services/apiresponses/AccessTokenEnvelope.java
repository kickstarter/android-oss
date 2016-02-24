package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.User;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class AccessTokenEnvelope implements Parcelable {
  public abstract String accessToken();
  public abstract User user();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder accessToken(String __);
    public abstract Builder user(User __);
    public abstract AccessTokenEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_AccessTokenEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}
