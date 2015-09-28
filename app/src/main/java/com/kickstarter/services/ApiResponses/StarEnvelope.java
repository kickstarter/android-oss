package com.kickstarter.services.apiresponses;

import android.os.Parcelable;

import com.kickstarter.libs.AutoGson;
import com.kickstarter.models.Project;
import com.kickstarter.models.User;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class StarEnvelope implements Parcelable {
  public abstract User user();
  public abstract Project project();
}
