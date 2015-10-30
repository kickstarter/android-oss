package com.kickstarter.services.apiresponses;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;
import com.kickstarter.models.pushdata.ActivityPushData;
import com.kickstarter.models.pushdata.GCMPushData;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class NotificationEnvelope implements Parcelable {
  @Nullable public abstract ActivityPushData activity();
  public abstract GCMPushData gcm();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder activity(ActivityPushData __);
    public abstract Builder gcm(GCMPushData __);
    public abstract NotificationEnvelope build();
  }

  public static Builder builder() {
    return new AutoParcel_NotificationEnvelope.Builder();
  }

  public abstract Builder toBuilder();
}

