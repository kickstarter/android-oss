package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.pushdata.Activity;
import com.kickstarter.models.pushdata.GCM;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class PushNotificationEnvelope implements Parcelable {
  @Nullable public abstract Activity activity();
  public abstract GCM gcm();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder activity(Activity __);
    public abstract Builder gcm(GCM __);
    public abstract PushNotificationEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_PushNotificationEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}

