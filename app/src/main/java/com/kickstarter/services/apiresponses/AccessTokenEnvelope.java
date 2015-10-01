package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;
import com.kickstarter.models.User;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class AccessTokenEnvelope implements Parcelable {
  public abstract String accessToken();
  public abstract User user();
}
