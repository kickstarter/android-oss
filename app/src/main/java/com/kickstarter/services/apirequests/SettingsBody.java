package com.kickstarter.services.apirequests;

import android.support.annotation.Nullable;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SettingsBody {
  public abstract @Nullable Boolean notifyMobileOfFollower();
  public abstract @Nullable Boolean notifyMobileOfFriendActivity();
  public abstract @Nullable Boolean notifyMobileOfUpdates();
  public abstract @Nullable Boolean notifyOfFollower();
  public abstract @Nullable Boolean notifyOfFriendActivity();
  public abstract @Nullable Boolean notifyOfUpdates();
  public abstract @Nullable Boolean sendHappeningNewsletter();
  public abstract @Nullable Boolean sendPromoNewsletter();
  public abstract @Nullable Boolean sendWeeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder notifyMobileOfFollower(Boolean __);
    public abstract Builder notifyMobileOfFriendActivity(Boolean __);
    public abstract Builder notifyMobileOfUpdates(Boolean __);
    public abstract Builder notifyOfFollower(Boolean __);
    public abstract Builder notifyOfFriendActivity(Boolean __);
    public abstract Builder notifyOfUpdates(Boolean __);
    public abstract Builder sendHappeningNewsletter(Boolean __);
    public abstract Builder sendPromoNewsletter(Boolean __);
    public abstract Builder sendWeeklyNewsletter(Boolean __);
    public abstract SettingsBody build();
  }

  public static Builder builder() {
    return new AutoParcel_SettingsBody.Builder();
  }

  public abstract Builder toBuilder();
}
