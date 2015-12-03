package com.kickstarter.services.apirequests;

import com.kickstarter.libs.qualifiers.AutoGson;

import auto.parcel.AutoParcel;

@AutoGson
@AutoParcel
public abstract class SettingsBody {
  public abstract boolean notifyMobileOfBackings();
  public abstract boolean notifyMobileOfComments();
  public abstract boolean notifyMobileOfFollower();
  public abstract boolean notifyMobileOfFriendActivity();
  public abstract boolean notifyMobileOfUpdates();
  public abstract boolean notifyOfBackings();
  public abstract boolean notifyOfComments();
  public abstract boolean notifyOfFollower();
  public abstract boolean notifyOfFriendActivity();
  public abstract boolean notifyOfUpdates();
  public abstract boolean sendHappeningNewsletter();
  public abstract boolean sendPromoNewsletter();
  public abstract boolean sendWeeklyNewsletter();

  @AutoParcel.Builder
  public abstract static class Builder {
    public abstract Builder notifyMobileOfBackings(boolean __);
    public abstract Builder notifyMobileOfComments(boolean __);
    public abstract Builder notifyMobileOfFollower(boolean __);
    public abstract Builder notifyMobileOfFriendActivity(boolean __);
    public abstract Builder notifyMobileOfUpdates(boolean __);
    public abstract Builder notifyOfBackings(boolean __);
    public abstract Builder notifyOfComments(boolean __);
    public abstract Builder notifyOfFollower(boolean __);
    public abstract Builder notifyOfFriendActivity(boolean __);
    public abstract Builder notifyOfUpdates(boolean __);
    public abstract Builder sendHappeningNewsletter(boolean __);
    public abstract Builder sendPromoNewsletter(boolean __);
    public abstract Builder sendWeeklyNewsletter(boolean __);
    public abstract SettingsBody build();
  }

  public static Builder builder() {
    return new AutoParcel_SettingsBody.Builder();
  }

  public abstract Builder toBuilder();
}
